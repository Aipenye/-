package com.wms.agv.logic;

import com.wms.agv.entity.AgvEntity;
import com.wms.agv.entity.AgvState;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 三级避障状态机（含迟滞，防止边界处状态抖动）
 * >3m          → RUNNING（正常速度）
 * 1.5~3m       → CAUTION（速度从 100% 线性降至 0%）
 * ≤1.5m        → EMERGENCY_STOP（急刹）
 *
 * 优先级让行：检测到对方 AGV 且对方 ID < 自己 ID 时，返回 YIELD_WAIT，
 * 让高优先级车先行，低优先级车原地等待。
 */
@Component
public class AvoidanceController {

    private static final double SAFE_DIST  = 3.0;
    private static final double STOP_DIST  = 1.5;
    private static final double HYSTERESIS = 0.3;

    public record AvoidanceResult(AgvState state, double speedMultiplier, boolean yieldToHigher) {}

    public AvoidanceResult evaluate(List<SensorSimulator.Detection> detections,
                                    AgvState currentState, int selfId) {
        if (detections.isEmpty()) {
            return new AvoidanceResult(AgvState.RUNNING, 1.0, false);
        }

        double minDist = Double.MAX_VALUE;
        boolean yieldToHigher = false;

        for (SensorSimulator.Detection d : detections) {
            if (d.distance() < minDist) minDist = d.distance();
            // 检测到对方是 AGV 且优先级更高（ID 更小）
            if (d.isAgv() && d.agvId() < selfId) {
                yieldToHigher = true;
            }
        }

        // 低优先级车检测到高优先级车在近距离内 → 让行等待
        if (yieldToHigher && minDist < SAFE_DIST) {
            return new AvoidanceResult(AgvState.WAITING_LOCK, 0.0, true);
        }

        // 急停
        if (minDist <= STOP_DIST) {
            return new AvoidanceResult(AgvState.EMERGENCY_STOP, 0.0, false);
        }

        // 从 EMERGENCY_STOP 回退迟滞
        if (currentState == AgvState.EMERGENCY_STOP && minDist < STOP_DIST + HYSTERESIS) {
            return new AvoidanceResult(AgvState.EMERGENCY_STOP, 0.0, false);
        }

        // CAUTION 区线性减速
        if (minDist < SAFE_DIST) {
            double ratio = (minDist - STOP_DIST) / (SAFE_DIST - STOP_DIST);
            double mult  = Math.max(0.0, ratio);
            return new AvoidanceResult(AgvState.CAUTION, mult, false);
        }

        // 从 CAUTION 回退迟滞
        if (currentState == AgvState.CAUTION && minDist < SAFE_DIST + HYSTERESIS) {
            double ratio = (minDist - STOP_DIST) / (SAFE_DIST - STOP_DIST);
            return new AvoidanceResult(AgvState.CAUTION, Math.max(0.0, ratio), false);
        }

        return new AvoidanceResult(AgvState.RUNNING, 1.0, false);
    }
}

