package com.wms.agv.logic;

import com.wms.agv.entity.AgvEntity;
import com.wms.agv.entity.AgvState;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 三级避障状态机（含迟滞，防止边界处状态抖动）
 * >3m         → RUNNING（正常速度）
 * 0.5~3m      → CAUTION（速度从 100% 线性降至 0%，在 0.5m 处恰好停止）
 * ≤0.5m       → EMERGENCY_STOP（急停，最终停止距离 = 0.5m）
 *
 * 迟滞规则：从高危向低危回退时阈值额外放宽 0.3m，防止边界抖动。
 */
@Component
public class AvoidanceController {

    private static final double SAFE_DIST    = 3.0;
    // 目标停止距离：小车最终停在距障碍物 0.5m 处
    private static final double STOP_DIST   = 0.5;
    private static final double HYSTERESIS  = 0.3;

    public record AvoidanceResult(AgvState state, double speedMultiplier) {}

    public AvoidanceResult evaluate(List<SensorSimulator.Detection> detections, AgvState currentState) {
        if (detections.isEmpty()) {
            return new AvoidanceResult(AgvState.RUNNING, 1.0);
        }
        double minDist = detections.stream()
                .mapToDouble(SensorSimulator.Detection::distance)
                .min().orElse(Double.MAX_VALUE);

        // 已到达或越过停止距离 → 急停
        if (minDist <= STOP_DIST) {
            return new AvoidanceResult(AgvState.EMERGENCY_STOP, 0.0);
        }

        // 从 EMERGENCY_STOP 回退需要距离 > 0.8m（迟滞防抖）
        if (currentState == AgvState.EMERGENCY_STOP && minDist < STOP_DIST + HYSTERESIS) {
            return new AvoidanceResult(AgvState.EMERGENCY_STOP, 0.0);
        }

        // CAUTION 区：速度从 SAFE_DIST 处 100% 线性降到 STOP_DIST 处 0%
        // 保证小车在 0.5m 处速度恰好为 0
        if (minDist < SAFE_DIST) {
            double ratio = (minDist - STOP_DIST) / (SAFE_DIST - STOP_DIST);
            double mult  = Math.max(0.0, ratio);
            return new AvoidanceResult(AgvState.CAUTION, mult);
        }

        // 从 CAUTION 回退到 RUNNING 需要距离 > 3.3m（迟滞防抖）
        if (currentState == AgvState.CAUTION && minDist < SAFE_DIST + HYSTERESIS) {
            double ratio = (minDist - STOP_DIST) / (SAFE_DIST - STOP_DIST);
            return new AvoidanceResult(AgvState.CAUTION, Math.max(0.0, ratio));
        }

        return new AvoidanceResult(AgvState.RUNNING, 1.0);
    }
}
