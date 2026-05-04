package com.wms.agv.logic;

import com.wms.agv.entity.AgvEntity;
import com.wms.agv.entity.AgvState;
import com.wms.agv.entity.SimWorker;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * 传感器模拟：210° 扇形视野，5m 探测半径
 * 返回视野内的工人列表及其距离
 */
@Component
public class SensorSimulator {

    private static final double DETECT_RADIUS = 5.0;
    private static final double HALF_FOV_DEG  = 105.0; // 210° / 2

    public record Detection(SimWorker worker, double distance) {}

    public List<Detection> detect(AgvEntity agv, List<SimWorker> workers) {
        List<Detection> result = new ArrayList<>();
        for (SimWorker w : workers) {
            double dx = w.getX() - agv.getX();
            double dy = w.getY() - agv.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > DETECT_RADIUS) continue;

            // 工人相对 AGV 的角度（度，0=东，90=北）
            double workerAngleDeg = Math.toDegrees(Math.atan2(dy, dx));
            double agvAngleDeg    = agv.getAngle();

            double diff = Math.abs(normalizeAngle(workerAngleDeg - agvAngleDeg));
            if (diff <= HALF_FOV_DEG) {
                result.add(new Detection(w, dist));
            }
        }
        return result;
    }

    private double normalizeAngle(double deg) {
        while (deg >  180) deg -= 360;
        while (deg < -180) deg += 360;
        return deg;
    }
}
