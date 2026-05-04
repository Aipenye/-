package com.wms.agv.entity;

import lombok.Data;
import java.util.List;

@Data
public class SimWorker {

    private int id;
    private double x;
    private double y;
    private double angle;       // 朝向（度）
    private double speed = 0.8; // 步行速度 m/s
    private double collisionRadius = 0.3;

    private WorkerMode mode = WorkerMode.SIMULATED;
    private WorkerState state = WorkerState.WALKING;

    // 巡逻路径点（米坐标）
    private List<double[]> waypoints;
    private int waypointIndex = 0;

    // 停顿计时（ms），>0 时处于 STOPPED 状态
    private long stopRemaining = 0;

    // ---- REAL 模式预留接口 ----
    // 当 mode == REAL 时，由外部调用 updateRealPosition() 注入坐标
    // 目前不启用，等待真实定位数据接入
    public void updateRealPosition(double x, double y) {
        if (mode == WorkerMode.REAL) {
            this.x = x;
            this.y = y;
        }
    }

    public double[] currentWaypoint() {
        if (waypoints == null || waypoints.isEmpty()) return null;
        return waypoints.get(waypointIndex % waypoints.size());
    }

    public void advanceWaypoint() {
        if (waypoints != null && !waypoints.isEmpty()) {
            waypointIndex = (waypointIndex + 1) % waypoints.size();
        }
    }
}
