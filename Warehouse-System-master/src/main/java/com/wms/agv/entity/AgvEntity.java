package com.wms.agv.entity;

import lombok.Data;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class AgvEntity {

    private int id;
    private double x;
    private double y;
    private double angle;
    private double speed;
    private boolean loaded;

    private AgvState state = AgvState.IDLE;

    private List<double[]> path = new ArrayList<>();
    private int pathIndex = 0;

    private double targetX;
    private double targetY;

    private long liftingRemaining = 0;
    private long waitingTick = 0;
    private long stopWaitingTick = 0;
    private long replanTick = 0;       // REVERSING 状态下每秒重规划计时

    /** 本车本次规划临时需要绕开的栅格坐标（col*1000+row 编码），任务完成时清空 */
    private final Set<Integer> tempObstacles = new HashSet<>();

    // ── 任务字段 ──────────────────────────────────────────────
    /** 当前执行的工单ID，-1表示无任务 */
    private int currentOrderId = -1;
    /** 目标货架编号（agvSlot），-1表示无 */
    private int targetSlot = -1;
    /** 停车位索引（0/1/2），决定停在哪个并列位 */
    private int parkSlotIndex = 0;

    public double getMaxSpeed() {
        return loaded ? 1.0 : 1.5;
    }

    public boolean hasPath() {
        return path != null && pathIndex < path.size();
    }

    public double[] currentWaypoint() {
        if (!hasPath()) return null;
        return path.get(pathIndex);
    }

    public void advanceWaypoint() {
        pathIndex++;
    }

    public boolean hasTask() {
        return currentOrderId >= 0;
    }
}
