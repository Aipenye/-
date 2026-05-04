package com.wms.agv.engine;

import com.wms.agv.dto.AgvDTO;
import com.wms.agv.dto.SimStateDTO;
import com.wms.agv.dto.WorkerDTO;
import com.wms.agv.entity.*;
import com.wms.agv.logic.AvoidanceController;
import com.wms.agv.logic.SensorSimulator;
import com.wms.agv.logic.TrafficManager;
import com.wms.agv.ws.AgvStatePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@EnableScheduling
public class AgvSimulationEngine {

    private static final long TICK_MS = 50;
    private static final double DT    = TICK_MS / 1000.0; // 0.05s

    @Autowired private WarehouseMap       map;
    @Autowired private AStarPathfinder    pathfinder;
    @Autowired private SensorSimulator    sensor;
    @Autowired private AvoidanceController avoidance;
    @Autowired private TrafficManager     traffic;
    @Autowired private AgvStatePublisher  publisher;

    private volatile boolean running = false;

    private final List<AgvEntity>  agvs    = new CopyOnWriteArrayList<>();
    private final List<SimWorker>  workers = new CopyOnWriteArrayList<>();

    // AGV 巡逻任务点（起点→货架→出库口循环）
    private static final double[][] AGV_WAYPOINTS_1 = {
        {40, 20}, {6, 3}, {40, 20}, {6, 7.5}, {40, 20}, {6, 12}, {40, 20}, {6, 16.5}
    };
    private static final double[][] AGV_WAYPOINTS_2 = {
        {40, 20}, {17, 23.5}, {40, 20}, {17, 28}, {40, 20}, {17, 32.5}, {40, 20}, {17, 37}
    };
    private static final double[][] AGV_WAYPOINTS_3 = {
        {40, 20}, {52, 3}, {40, 20}, {52, 7.5}, {40, 20}, {63, 12}, {40, 20}, {74, 16.5}
    };

    // 工人巡逻路径点
    private static final double[][] WORKER_WAYPOINTS_1 = {
        {6, 3}, {6, 7.5}, {17, 7.5}, {17, 3}, {6, 3}
    };
    private static final double[][] WORKER_WAYPOINTS_2 = {
        {52, 23.5}, {52, 28}, {63, 28}, {63, 23.5}, {74, 23.5}, {74, 28}, {52, 23.5}
    };
    private static final double[][] WORKER_WAYPOINTS_3 = {
        {28, 3}, {28, 7.5}, {28, 12}, {28, 16.5}, {28, 12}, {28, 7.5}, {28, 3}
    };

    // 每个 AGV 的任务点索引
    private final int[] agvTaskIndex = {0, 0, 0};

    @PostConstruct
    public void init() {
        initAgvs();
        initWorkers();
    }

    private void initAgvs() {
        agvs.clear();
        AgvEntity a1 = new AgvEntity(); a1.setId(1); a1.setX(40); a1.setY(5);  a1.setAngle(90);
        AgvEntity a2 = new AgvEntity(); a2.setId(2); a2.setX(40); a2.setY(20); a2.setAngle(0);
        AgvEntity a3 = new AgvEntity(); a3.setId(3); a3.setX(40); a3.setY(35); a3.setAngle(270);
        agvs.addAll(List.of(a1, a2, a3));
        agvTaskIndex[0] = 0; agvTaskIndex[1] = 0; agvTaskIndex[2] = 0;
    }

    private void initWorkers() {
        workers.clear();
        workers.add(buildWorker(1, 6,  3,  WORKER_WAYPOINTS_1));
        workers.add(buildWorker(2, 52, 23.5, WORKER_WAYPOINTS_2));
        workers.add(buildWorker(3, 28, 3,  WORKER_WAYPOINTS_3));
    }

    private SimWorker buildWorker(int id, double x, double y, double[][] wps) {
        SimWorker w = new SimWorker();
        w.setId(id); w.setX(x); w.setY(y);
        List<double[]> list = new ArrayList<>();
        for (double[] wp : wps) list.add(wp);
        w.setWaypoints(list);
        w.setMode(WorkerMode.SIMULATED);
        w.setState(WorkerState.WALKING);
        return w;
    }

    // ── 主循环 ──────────────────────────────────────────────

    @Scheduled(fixedRate = TICK_MS)
    public void tick() {
        if (!running) return;
        updateWorkers();
        updateAgvs();
        publisher.publish(buildState());
    }

    // ── 工人更新 ─────────────────────────────────────────────

    private void updateWorkers() {
        for (SimWorker w : workers) {
            if (w.getMode() == WorkerMode.REAL) continue; // REAL 模式由外部注入，跳过

            if (w.getStopRemaining() > 0) {
                w.setStopRemaining(w.getStopRemaining() - TICK_MS);
                if (w.getStopRemaining() <= 0) {
                    w.setStopRemaining(0);
                    w.setState(WorkerState.WALKING);
                    w.advanceWaypoint();
                }
                continue;
            }

            double[] target = w.currentWaypoint();
            if (target == null) continue;

            double dx = target[0] - w.getX();
            double dy = target[1] - w.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < 0.2) {
                // 到达路径点，随机停顿 1~5s 模拟拣货
                long stopMs = 1000 + (long)(Math.random() * 4000);
                w.setStopRemaining(stopMs);
                w.setState(WorkerState.STOPPED);
            } else {
                double step = w.getSpeed() * DT;
                w.setX(w.getX() + dx / dist * step);
                w.setY(w.getY() + dy / dist * step);
                w.setAngle(Math.toDegrees(Math.atan2(dy, dx)));
                w.setState(WorkerState.WALKING);
            }
        }
    }

    // ── AGV 更新 ──────────────────────────────────────────────

    private void updateAgvs() {
        for (int i = 0; i < agvs.size(); i++) {
            AgvEntity agv = agvs.get(i);
            ensurePath(agv, i);
            if (!agv.hasPath()) { agv.setState(AgvState.IDLE); continue; }

            // 举升中
            if (agv.getState() == AgvState.LIFTING) {
                agv.setLiftingRemaining(agv.getLiftingRemaining() - TICK_MS);
                if (agv.getLiftingRemaining() <= 0) {
                    agv.setLoaded(!agv.isLoaded());
                    agv.setState(AgvState.RUNNING);
                    agvTaskIndex[i] = (agvTaskIndex[i] + 1) % getWaypoints(i).length;
                    planPath(agv, i);
                }
                continue;
            }

            // 感知 + 避障
            List<SensorSimulator.Detection> detections = sensor.detect(agv, workers);
            AvoidanceController.AvoidanceResult ar = avoidance.evaluate(detections);

            if (ar.state() == AgvState.EMERGENCY_STOP) {
                agv.setState(AgvState.EMERGENCY_STOP);
                agv.setSpeed(0);
                continue;
            }

            // 通道锁检查
            String zone = traffic.resolveZone(agv.getX(), agv.getY());
            if (zone != null && traffic.isBlockedByOther(zone, agv.getId())) {
                agv.setState(AgvState.WAITING_LOCK);
                agv.setSpeed(0);
                continue;
            }
            if (zone != null) traffic.tryEnter(zone, agv.getId());

            // 移动
            agv.setState(ar.state());
            double[] wp = agv.currentWaypoint();
            double dx = wp[0] - agv.getX();
            double dy = wp[1] - agv.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            double targetSpeed = agv.getMaxSpeed() * ar.speedMultiplier();
            // 加速度限制 0.5 m/s²
            double curSpeed = agv.getSpeed();
            double maxDelta  = 0.5 * DT;
            double newSpeed  = curSpeed + Math.signum(targetSpeed - curSpeed) * Math.min(maxDelta, Math.abs(targetSpeed - curSpeed));
            agv.setSpeed(newSpeed);

            double step = newSpeed * DT;
            if (dist <= step) {
                agv.setX(wp[0]);
                agv.setY(wp[1]);
                agv.advanceWaypoint();

                if (!agv.hasPath()) {
                    // 到达最终目标，触发举升
                    agv.setState(AgvState.LIFTING);
                    agv.setLiftingRemaining(3000);
                    agv.setSpeed(0);
                }
            } else {
                agv.setX(agv.getX() + dx / dist * step);
                agv.setY(agv.getY() + dy / dist * step);
                agv.setAngle(Math.toDegrees(Math.atan2(dy, dx)));
            }

            // 释放已离开的通道锁
            String curZone = traffic.resolveZone(agv.getX(), agv.getY());
            if (zone != null && !zone.equals(curZone)) {
                traffic.release(zone, agv.getId());
            }
        }
    }

    private void ensurePath(AgvEntity agv, int idx) {
        if (agv.hasPath() || agv.getState() == AgvState.LIFTING) return;
        planPath(agv, idx);
    }

    private void planPath(AgvEntity agv, int idx) {
        double[][] wps = getWaypoints(idx);
        int ti = agvTaskIndex[idx] % wps.length;
        double[] target = wps[ti];
        List<double[]> path = pathfinder.findPath(agv.getX(), agv.getY(), target[0], target[1]);
        agv.setPath(path);
        agv.setPathIndex(0);
        agv.setTargetX(target[0]);
        agv.setTargetY(target[1]);
        if (!path.isEmpty()) agv.setState(AgvState.RUNNING);
    }

    private double[][] getWaypoints(int agvIdx) {
        return switch (agvIdx) {
            case 0 -> AGV_WAYPOINTS_1;
            case 1 -> AGV_WAYPOINTS_2;
            default -> AGV_WAYPOINTS_3;
        };
    }

    // ── 状态序列化 ────────────────────────────────────────────

    private SimStateDTO buildState() {
        SimStateDTO dto = new SimStateDTO();
        dto.setTimestamp(System.currentTimeMillis());
        dto.setRunning(running);

        List<AgvDTO> agvDTOs = new ArrayList<>();
        for (AgvEntity a : agvs) {
            AgvDTO d = new AgvDTO();
            d.setId(a.getId()); d.setX(a.getX()); d.setY(a.getY());
            d.setAngle(a.getAngle()); d.setSpeed(a.getSpeed());
            d.setState(a.getState().name()); d.setLoaded(a.isLoaded());
            // 只传剩余路径（最多 20 个点，避免数据包过大）
            List<double[]> remaining = a.getPath() == null ? Collections.emptyList()
                    : a.getPath().subList(Math.min(a.getPathIndex(), a.getPath().size()),
                                          Math.min(a.getPathIndex() + 20, a.getPath().size()));
            d.setPath(remaining);
            agvDTOs.add(d);
        }
        dto.setAgvs(agvDTOs);

        List<WorkerDTO> wDTOs = new ArrayList<>();
        for (SimWorker w : workers) {
            WorkerDTO d = new WorkerDTO();
            d.setId(w.getId()); d.setX(w.getX()); d.setY(w.getY());
            d.setAngle(w.getAngle()); d.setMode(w.getMode().name());
            d.setState(w.getState().name());
            wDTOs.add(d);
        }
        dto.setWorkers(wDTOs);
        return dto;
    }

    // ── 控制接口 ──────────────────────────────────────────────

    public void start()  { running = true; }
    public void stop()   { running = false; }
    public boolean isRunning() { return running; }

    public void reset() {
        running = false;
        initAgvs();
        initWorkers();
    }

    public SimStateDTO getSnapshot() { return buildState(); }
}
