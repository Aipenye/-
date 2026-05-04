package com.wms.agv.logic;

import com.wms.agv.entity.AgvEntity;
import com.wms.agv.entity.AgvState;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 三级避障状态机
 * >3m  → RUNNING（正常）
 * 1~3m → CAUTION（减速至 30%）
 * <1m  → EMERGENCY_STOP（急停）
 */
@Component
public class AvoidanceController {

    private static final double SAFE_DIST      = 3.0;
    private static final double CAUTION_DIST   = 1.0;

    public record AvoidanceResult(AgvState state, double speedMultiplier) {}

    public AvoidanceResult evaluate(List<SensorSimulator.Detection> detections) {
        if (detections.isEmpty()) {
            return new AvoidanceResult(AgvState.RUNNING, 1.0);
        }
        double minDist = detections.stream()
                .mapToDouble(SensorSimulator.Detection::distance)
                .min().orElse(Double.MAX_VALUE);

        if (minDist < CAUTION_DIST) {
            return new AvoidanceResult(AgvState.EMERGENCY_STOP, 0.0);
        } else if (minDist < SAFE_DIST) {
            // 线性减速：3m→100%，1m→30%
            double ratio = (minDist - CAUTION_DIST) / (SAFE_DIST - CAUTION_DIST);
            double mult  = 0.3 + ratio * 0.7;
            return new AvoidanceResult(AgvState.CAUTION, mult);
        }
        return new AvoidanceResult(AgvState.RUNNING, 1.0);
    }
}
