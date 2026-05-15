package com.wms.agv.engine;

import com.wms.agv.dto.AgvDTO;
import com.wms.agv.dto.SimStateDTO;
import com.wms.agv.dto.WorkerDTO;
import com.wms.agv.entity.*;
import com.wms.agv.logic.AvoidanceController;
import com.wms.agv.logic.SensorSimulator;
import com.wms.agv.logic.TrafficManager;
import com.wms.agv.ws.AgvStatePublisher;
import com.wms.entity.Goods;
import com.wms.entity.Storage;
import com.wms.entity.WorkOrder;
import com.wms.service.GoodsService;
import com.wms.service.RecordService;
import com.wms.service.StorageService;
import com.wms.service.WorkOrderService;
import com.wms.service.BoxOptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
@EnableScheduling
public class AgvSimulationEngine {

    private static final long   TICK_MS = 50;
    private static final double DT      = TICK_MS / 1000.0;

    @Autowired private WarehouseMap        map;
    @Autowired private AStarPathfinder     pathfinder;
    @Autowired private SensorSimulator     sensor;
    @Autowired private AvoidanceController avoidance;
    @Autowired private TrafficManager      traffic;
    @Autowired private AgvStatePublisher   publisher;

    @Autowired private WorkOrderService workOrderService;
    @Autowired private GoodsService     goodsService;
    @Autowired private StorageService   storageService;
    @Autowired private RecordService    recordService;
    @Autowired private BoxOptimizationService boxOptimizationService;

    private volatile boolean running = true;

    private final List<AgvEntity> agvs    = new CopyOnWriteArrayList<>();
    private final List<SimWorker> workers = new CopyOnWriteArrayList<>();

    // 工人巡逻路径点（保持模拟不变）
    private static final double[][] WORKER_WAYPOINTS_1 = {
        {6, 3}, {6, 7.5}, {17, 7.5}, {17, 3}, {6, 3}
    };
    private static final double[][] WORKER_WAYPOINTS_2 = {
        {52, 23.5}, {52, 28}, {63, 28}, {63, 23.5}, {74, 23.5}, {74, 28}, {52, 23.5}
    };
    private static final double[][] WORKER_WAYPOINTS_3 = {
        {28, 3}, {28, 7.5}, {28, 12}, {28, 16.5}, {28, 12}, {28, 7.5}, {28, 3}
    };

    @PostConstruct
    public void init() {
        initAgvs();
        initWorkers();
        syncStorageSlots();
    }

    private void initAgvs() {
        agvs.clear();
        double[][] slots = WarehouseMap.PARK_SLOTS;
        for (int i = 0; i < 3; i++) {
            AgvEntity a = new AgvEntity();
            a.setId(i + 1);
            a.setX(slots[i][0]);
            a.setY(slots[i][1]);
            a.setAngle(90); // 朝北
            a.setParkSlotIndex(i);
            a.setState(AgvState.IDLE);
            agvs.add(a);
        }
        // 第4个停车位（index=3）预留，暂无AGV
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

    /** 启动时同步数据库中已注册的货架编号到地图 */
    public void syncStorageSlots() {
        try {
            List<Storage> all = storageService.list();
            Set<Integer> slots = all.stream()
                    .filter(s -> s.getAgvSlot() != null)
                    .map(Storage::getAgvSlot)
                    .collect(Collectors.toSet());
            map.syncRegisteredSlots(slots);
        } catch (Exception ignored) {}
    }

    // ── 主循环 ──────────────────────────────────────────────

    @Scheduled(fixedRate = TICK_MS)
    public void tick() {
        if (!running) return;
        updateWorkers();
        updateAgvs();
        publisher.publish(buildState());
    }

    // ── 工人更新（模拟不变）────────────────────────────────────

    private void updateWorkers() {
        for (SimWorker w : workers) {
            if (w.getMode() == WorkerMode.REAL) continue;
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
        for (AgvEntity agv : agvs) {

            // 举升中（必须在 hasPath 检查之前）
            if (agv.getState() == AgvState.LIFTING) {
                agv.setLiftingRemaining(agv.getLiftingRemaining() - TICK_MS);
                if (agv.getLiftingRemaining() <= 0) {
                    onLiftingComplete(agv);
                }
                continue;
            }

            // 倒车入库中：朝北(90°)缓慢向南倒入停车位
            if (agv.getState() == AgvState.PARKING) {
                handleParkingState(agv);
                continue;
            }

            // 无路径时：IDLE停在停车区，或尝试接收新任务（倒车中跳过）
            if (!agv.hasPath() && agv.getState() != AgvState.REVERSING) {
                if (agv.getState() == AgvState.RETURNING) {
                    // 到达出入口点，切换为倒车入库
                    agv.setState(AgvState.PARKING);
                    agv.setAngle(90); // 朝北，倒车方向为正南
                    agv.setSpeed(0);
                    handleParkingState(agv);
                    continue;
                }
                if (agv.getState() == AgvState.IDLE && !agv.hasTask()) {
                    // 尝试从数据库拉取待处理的AGV工单
                    tryAssignTask(agv);
                }
                if (!agv.hasPath()) {
                    agv.setState(AgvState.IDLE);
                    continue;
                }
            }

            // 感知 + 避障
            List<AgvEntity> otherAgvs = agvs.stream()
                    .filter(a -> a.getId() != agv.getId())
                    .collect(Collectors.toList());
            List<SensorSimulator.Detection> detections = sensor.detect(agv, workers, otherAgvs);
            AvoidanceController.AvoidanceResult ar = avoidance.evaluate(detections, agv.getState(), agv.getId());

            // 让行：检测到高优先级 AGV（ID 更小），原地等待直到对方离开
            if (ar.yieldToHigher()) {
                agv.setState(AgvState.WAITING_LOCK);
                agv.setSpeed(0);
                agv.setWaitingTick(0);
                continue;
            }

            // 判断触发停止的最近障碍是否全是 AGV（移动障碍）
            boolean blockedByAgvOnly = !detections.isEmpty() && detections.stream()
                    .filter(d -> d.distance() < 3.0)
                    .allMatch(SensorSimulator.Detection::isAgv);

            // 被其他 AGV 阻挡：只等待，不倒车不标记临时障碍，等对方移开后自然恢复
            if (blockedByAgvOnly && (ar.state() == AgvState.EMERGENCY_STOP
                    || (ar.state() == AgvState.CAUTION && ar.speedMultiplier() < 0.05))) {
                agv.setState(AgvState.WAITING_LOCK);
                agv.setSpeed(0);
                agv.setWaitingTick(agv.getWaitingTick() + TICK_MS);
                // 等待超过 8s 仍未通行，说明对方也卡住了，尝试重规划（不标记临时障碍）
                if (agv.getWaitingTick() > 8000) {
                    agv.setWaitingTick(0);
                    replanCurrentPath(agv);
                }
                continue;
            }

            if (ar.state() == AgvState.EMERGENCY_STOP
                    || agv.getState() == AgvState.REVERSING
                    || (ar.state() == AgvState.CAUTION && ar.speedMultiplier() < 0.05)) {

                // 进入倒车状态：仅对静态障碍（工人）标记临时障碍
                if (agv.getState() != AgvState.REVERSING) {
                    agv.setState(AgvState.REVERSING);
                    agv.setStopWaitingTick(0);
                    agv.setReplanTick(0);
                    markTempObstaclesAhead(agv);
                }

                agv.setStopWaitingTick(agv.getStopWaitingTick() + TICK_MS);
                agv.setReplanTick(agv.getReplanTick() + TICK_MS);

                // 每秒尝试重规划一次
                if (agv.getReplanTick() >= 1000) {
                    agv.setReplanTick(0);
                    // 每辆车超时阈值错开，防止两车同时重规划
                    long stopThreshold = 10000L + (agv.getId() - 1) * 2000L;
                    if (agv.getStopWaitingTick() >= stopThreshold) {
                        agv.setStopWaitingTick(0);
                        replanCurrentPath(agv);
                    } else {
                        // 尝试重规划：若新路径前方无障碍则立即前进
                        List<double[]> newPath = tryReplan(agv);
                        if (newPath != null) {
                            agv.setPath(newPath);
                            agv.setPathIndex(0);
                            agv.setState(agv.hasTask() ? AgvState.RUNNING : AgvState.RETURNING);
                            agv.setStopWaitingTick(0);
                            agv.setReplanTick(0);
                            continue;
                        }
                    }
                }

                // 倒车：沿当前朝向反方向缓慢移动
                double reverseSpeed = 0.3;
                double rad = Math.toRadians(agv.getAngle());
                double nx = agv.getX() - Math.cos(rad) * reverseSpeed * DT;
                double ny = agv.getY() - Math.sin(rad) * reverseSpeed * DT;
                // 边界检查，不倒出地图
                if (nx > 1.5 && nx < 78.5 && ny > 1.5 && ny < 38.5) {
                    agv.setX(nx);
                    agv.setY(ny);
                }
                agv.setSpeed(reverseSpeed);
                continue;
            }
            // 离开 EMERGENCY_STOP/REVERSING 时重置计时器
            agv.setStopWaitingTick(0);
            agv.setReplanTick(0);

            // 通道锁检查
            String zone = traffic.resolveZone(agv.getX(), agv.getY());
            if (zone != null && traffic.isBlockedByOther(zone, agv.getId())) {
                agv.setState(AgvState.WAITING_LOCK);
                agv.setSpeed(0);
                agv.setWaitingTick(agv.getWaitingTick() + TICK_MS);
                if (agv.getWaitingTick() > 5000) {
                    agv.setWaitingTick(0);
                    replanCurrentPath(agv);
                }
                continue;
            }
            agv.setWaitingTick(0);
            if (zone != null) traffic.tryEnter(zone, agv.getId());

            // 移动（保留 RETURNING 状态，不被 RUNNING 覆盖）
            if (ar.state() == AgvState.CAUTION) {
                agv.setState(AgvState.CAUTION);
            } else if (agv.getState() != AgvState.RETURNING
                    && agv.getState() != AgvState.REVERSING
                    && agv.getState() != AgvState.EXITING_PARK) {
                agv.setState(AgvState.RUNNING);
            }

            double[] wp = agv.currentWaypoint();
            double dx = wp[0] - agv.getX();
            double dy = wp[1] - agv.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            double targetSpeed = agv.getMaxSpeed() * ar.speedMultiplier();
            double curSpeed  = agv.getSpeed();
            double maxDelta  = 0.5 * DT;
            double newSpeed  = curSpeed + Math.signum(targetSpeed - curSpeed)
                    * Math.min(maxDelta, Math.abs(targetSpeed - curSpeed));
            agv.setSpeed(newSpeed);

            double step = newSpeed * DT;
            if (dist <= step) {
                agv.setX(wp[0]);
                agv.setY(wp[1]);
                agv.advanceWaypoint();

                // 驶出停车位：到达出入口点后切换为正常行驶
                if (agv.getState() == AgvState.EXITING_PARK && agv.hasPath()) {
                    agv.setState(AgvState.RUNNING);
                }

                if (!agv.hasPath()) {
                    // 到达终点
                    if (agv.hasTask()) {
                        // 到达货架，开始举升
                        agv.setState(AgvState.LIFTING);
                        agv.setLiftingRemaining(20000);
                        agv.setSpeed(0);
                    } else {
                        // 到达出入口点（RETURNING终点），切换为倒车入库
                        agv.setState(AgvState.PARKING);
                        agv.setAngle(90); // 朝北，倒车方向为正南
                        agv.setSpeed(0);
                    }
                }
            } else {
                agv.setX(agv.getX() + dx / dist * step);
                agv.setY(agv.getY() + dy / dist * step);
                agv.setAngle(Math.toDegrees(Math.atan2(dy, dx)));
            }

            // 释放通道锁
            String curZone = traffic.resolveZone(agv.getX(), agv.getY());
            if (zone != null && !zone.equals(curZone)) {
                traffic.release(zone, agv.getId());
            }
        }
    }

    // ── 举升完成回调 ──────────────────────────────────────────

    private void onLiftingComplete(AgvEntity agv) {
        agv.setLoaded(!agv.isLoaded());
        agv.setSpeed(0);
        agv.getTempObstacles().clear(); // 任务阶段完成，清空临时障碍

        if (agv.hasTask()) {
            // 完成工单：更新库存，然后返回停车区出入口点
            finishOrder(agv);
            agv.setCurrentOrderId(-1);
            agv.setTargetSlot(-1);
            // 规划回停车位出入口点的路径（到达后再倒车入库）
            double[] exitPt = WarehouseMap.PARK_EXIT_POINTS[agv.getParkSlotIndex()];
            List<double[]> path = pathfinder.findPath(agv.getX(), agv.getY(), exitPt[0], exitPt[1]);
            if (path != null && !path.isEmpty()) {
                agv.setPath(path);
                agv.setPathIndex(0);
                agv.setState(AgvState.RETURNING);
            } else {
                agv.setState(AgvState.IDLE);
            }
        } else {
            agv.setState(AgvState.IDLE);
        }
    }

    /** 完成工单：更新货物库存和仓库使用量 */
    private void finishOrder(AgvEntity agv) {
        WorkOrder order;
        try {
            order = workOrderService.getById(agv.getCurrentOrderId());
            if (order == null || (order.getStatus() != null && order.getStatus() == 1)) return;
        } catch (Exception e) {
            return;
        }

        // 关键一步：先把工单置为已完成，避免 AGV 回停车位后又把同一工单捞起来重做
        try {
            order.setStatus(1);
            order.setFinishTime(LocalDateTime.now());
            workOrderService.updateById(order);
        } catch (Exception e) {
            return;
        }

        // 后续的库存/记录更新失败不应影响工单完成状态
        try {
            int delta = (order.getType() != null && order.getType() == 1)
                    ? -order.getCount() : order.getCount();

            Goods goods = goodsService.getById(order.getGoodsId());
            if (goods != null) {
                int curCount = goods.getCount() == null ? 0 : goods.getCount();
                goods.setStorage(order.getStorageId());
                goods.setCount(Math.max(0, curCount + delta));
                goodsService.updateById(goods);
            }

            Storage storage = storageService.getById(order.getStorageId());
            if (storage != null) {
                int used = (storage.getUsed() == null ? 0 : storage.getUsed()) + delta;
                storage.setUsed(Math.max(0, used));
                storageService.updateById(storage);
            }

            com.wms.entity.Record record = new com.wms.entity.Record();
            record.setGoods(order.getGoodsId());
            record.setAdminId(order.getAdminId());
            record.setCount(delta);
            record.setCreatetime(LocalDateTime.now());
            record.setRemark("AGV-" + agv.getId() + " 工单#" + order.getId()
                    + (delta > 0 ? " 完成入库" : " 完成出库"));
            recordService.save(record);

            // 异步触发装箱优化，更新该仓库的3D布局缓存
            boxOptimizationService.optimizeAndSave(order.getStorageId());
        } catch (Exception e) {
            // 日志记录，不影响仿真
        }
    }

    // ── 任务分配 ──────────────────────────────────────────────

    /** 尝试从数据库拉取一条待处理的AGV工单并分配给该AGV */
    private void tryAssignTask(AgvEntity agv) {
        try {
            // 优先接取明确指派给本车的工单，其次接取尚未分配小车的工单
            WorkOrder order = workOrderService.lambdaQuery()
                    .eq(WorkOrder::getAssignedTo, 1)
                    .eq(WorkOrder::getStatus, 0)
                    .eq(WorkOrder::getAgvId, agv.getId())
                    .orderByAsc(WorkOrder::getCreateTime)
                    .last("LIMIT 1")
                    .one();
            if (order == null) {
                order = workOrderService.lambdaQuery()
                        .eq(WorkOrder::getAssignedTo, 1)
                        .eq(WorkOrder::getStatus, 0)
                        .isNull(WorkOrder::getAgvId)
                        .orderByAsc(WorkOrder::getCreateTime)
                        .last("LIMIT 1")
                        .one();
            }
            if (order == null) return;

            Storage storage = storageService.getById(order.getStorageId());
            if (storage == null || storage.getAgvSlot() == null) return;

            int slot = storage.getAgvSlot();
            double[] target = map.getSlotApproachPoint(slot);
            if (target == null) return;

            // 先规划从出入口点到目标的路径，再在路径头部插入出入口点
            double[] exitPt = WarehouseMap.PARK_EXIT_POINTS[agv.getParkSlotIndex()];
            List<double[]> pathToTarget = pathfinder.findPath(exitPt[0], exitPt[1], target[0], target[1]);
            if (pathToTarget.isEmpty()) return;
            List<double[]> fullPath = new ArrayList<>();
            fullPath.add(exitPt);
            fullPath.addAll(pathToTarget);

            // 将工单的 agv_id 更新为本车，防止其他车重复接取
            if (order.getAgvId() == null) {
                order.setAgvId(agv.getId());
                workOrderService.updateById(order);
            }

            agv.setPath(fullPath);
            agv.setPathIndex(0);
            agv.setCurrentOrderId(order.getId());
            agv.setTargetSlot(slot);
            agv.setTargetX(target[0]);
            agv.setTargetY(target[1]);
            agv.setState(AgvState.EXITING_PARK);
        } catch (Exception ignored) {}
    }

    /** 重规划当前目标路径（WAITING_LOCK超时时调用） */
    private void replanCurrentPath(AgvEntity agv) {
        List<double[]> path = tryReplan(agv);
        if (path != null) {
            agv.setPath(path);
            agv.setPathIndex(0);
        }
    }

    /**
     * 尝试重规划路径，返回新路径；若路径为空则返回 null。
     * 倒车期间每秒调用一次，路径可用时立即切换前进。
     * 使用本车的临时障碍集合，绕开上次卡住的区域。
     */
    private List<double[]> tryReplan(AgvEntity agv) {
        double tx, ty;
        if (agv.hasTask()) {
            double[] pt = map.getSlotApproachPoint(agv.getTargetSlot());
            if (pt == null) return null;
            tx = pt[0]; ty = pt[1];
        } else {
            double[] exitPt = WarehouseMap.PARK_EXIT_POINTS[agv.getParkSlotIndex()];
            tx = exitPt[0]; ty = exitPt[1];
        }
        List<double[]> path = pathfinder.findPath(agv.getX(), agv.getY(), tx, ty,
                agv.getTempObstacles());
        return path.isEmpty() ? null : path;
    }

    /**
     * 倒车入库：AGV朝北(90°)，向正南缓慢倒入停车位。
     * 到达停车位后设为IDLE。
     */
    private void handleParkingState(AgvEntity agv) {
        double[] slot = WarehouseMap.PARK_SLOTS[agv.getParkSlotIndex()];
        double dy = slot[1] - agv.getY(); // 正值表示目标在北，负值表示目标在南

        // 倒车方向为正南（y减小），当 agv.y <= slot[1] 时说明已到达或越过目标
        if (agv.getY() <= slot[1] + 0.05) {
            agv.setX(slot[0]);
            agv.setY(slot[1]);
            agv.setState(AgvState.IDLE);
            agv.setSpeed(0);
            agv.setAngle(90);
        } else {
            double reverseSpeed = 0.4;
            double step = reverseSpeed * DT;
            // 防止越过目标点：若剩余距离小于一步，直接到位
            double remaining = agv.getY() - slot[1];
            double moveY = Math.min(step, remaining);
            agv.setX(slot[0]);
            agv.setY(agv.getY() - moveY);
            agv.setAngle(90);
            agv.setSpeed(reverseSpeed);
        }
    }

    /**
     * 以前方中心线为轴，向两侧各扩展 AGV_HALF_WIDTH，形成宽禁区。
     */
    private static final double AGV_HALF_WIDTH = 1.5; // 小车半宽 + 障碍物半宽，单位 m

    private void markTempObstaclesAhead(AgvEntity agv) {
        double rad = Math.toRadians(agv.getAngle());
        double perpRad = rad + Math.PI / 2; // 垂直方向

        // 前方 0.5~5m，每 0.5m 采样一次
        for (double dist = 0.5; dist <= 5.0; dist += 0.5) {
            double cx = agv.getX() + Math.cos(rad) * dist;
            double cy = agv.getY() + Math.sin(rad) * dist;

            // 以中心点向两侧各扩展 AGV_HALF_WIDTH
            for (double side = -AGV_HALF_WIDTH; side <= AGV_HALF_WIDTH; side += 0.5) {
                double ox = cx + Math.cos(perpRad) * side;
                double oy = cy + Math.sin(perpRad) * side;
                int col = map.meterToCol(ox);
                int row = map.meterToRow(oy);
                if (col >= 0 && col < WarehouseMap.COLS
                        && row >= 0 && row < WarehouseMap.ROWS
                        && !map.isObstacle(col, row)) { // 不覆盖已有永久障碍
                    agv.getTempObstacles().add(col * 1000 + row);
                }
            }
        }
    }

    // ── 外部接口：派发AGV工单 ─────────────────────────────────

    /**
     * 由 WorkOrderController 调用，将工单分配给一辆空闲AGV。
     * 返回分配的AGV编号，-1表示无空闲AGV。
     */
    public int assignOrderToAgv(int orderId) {
        for (AgvEntity agv : agvs) {
            if (agv.getState() == AgvState.IDLE && !agv.hasTask()) {
                try {
                    WorkOrder order = workOrderService.getById(orderId);
                    if (order == null) return -1;
                    Storage storage = storageService.getById(order.getStorageId());
                    if (storage == null || storage.getAgvSlot() == null) return -1;

                    int slot = storage.getAgvSlot();
                    double[] target = map.getSlotApproachPoint(slot);
                    if (target == null) return -1;

                    double[] exitPt = WarehouseMap.PARK_EXIT_POINTS[agv.getParkSlotIndex()];
                    List<double[]> pathToTarget = pathfinder.findPath(exitPt[0], exitPt[1], target[0], target[1]);
                    if (pathToTarget.isEmpty()) continue;
                    List<double[]> fullPath = new ArrayList<>();
                    fullPath.add(exitPt);
                    fullPath.addAll(pathToTarget);

                    agv.setPath(fullPath);
                    agv.setPathIndex(0);
                    agv.setCurrentOrderId(orderId);
                    agv.setTargetSlot(slot);
                    agv.setTargetX(target[0]);
                    agv.setTargetY(target[1]);
                    agv.setState(AgvState.EXITING_PARK);
                    return agv.getId();
                } catch (Exception e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    /** 查询空闲AGV数量 */
    public long countIdleAgvs() {
        return agvs.stream().filter(a -> a.getState() == AgvState.IDLE && !a.hasTask()).count();
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
            d.setCurrentOrderId(a.getCurrentOrderId());
            d.setTargetSlot(a.getTargetSlot());
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
        traffic.releaseAll();
        initAgvs();
        initWorkers();
    }

    public SimStateDTO getSnapshot() { return buildState(); }
}
