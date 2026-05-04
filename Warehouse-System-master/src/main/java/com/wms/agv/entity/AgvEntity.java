package com.wms.agv.entity;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AgvEntity {

    private int id;
    private double x;          // 当前位置 x（米）
    private double y;          // 当前位置 y（米）
    private double angle;      // 朝向角度（度，0=东，90=北）
    private double speed;      // 当前速度（m/s）
    private boolean loaded;    // 是否满载

    private AgvState state = AgvState.IDLE;

    // A* 规划的路径点列表（米坐标）
    private List<double[]> path = new ArrayList<>();
    private int pathIndex = 0;

    // 最终目标
    private double targetX;
    private double targetY;

    // 举升计时（ms）
    private long liftingRemaining = 0;

    // 等待通道锁的计时（ms）
    private long waitingTick = 0;

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
}
