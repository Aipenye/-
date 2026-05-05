package com.wms.agv.logic;

import com.wms.agv.entity.AgvEntity;
import com.wms.agv.entity.SimWorker;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * 传感器模拟：210° 扇形视野，5m 探测半径
 * 同时检测工人和其他 AGV，返回视野内障碍物的距离列表
 */
@Component
public class SensorSimulator {

    private static final double DETECT_RADIUS = 5.0;
    private static final double HALF_FOV_DEG  = 105.0;

    /**
     * agvId = -1 表示工人，>0 表示对方 AGV 的 ID
     */
    public record Detection(double distance, int agvId) {
        public boolean isAgv() { return agvId > 0; }
    }

    public List<Detection> detect(AgvEntity agv, List<SimWorker> workers, List<AgvEntity> otherAgvs) {
        List<Detection> result = new ArrayList<>();

        for (SimWorker w : workers) {
            double dx = w.getX() - agv.getX();
            double dy = w.getY() - agv.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > DETECT_RADIUS) continue;
            double diff = Math.abs(normalizeAngle(Math.toDegrees(Math.atan2(dy, dx)) - agv.getAngle()));
            if (diff <= HALF_FOV_DEG) result.add(new Detection(dist, -1));
        }

        for (AgvEntity other : otherAgvs) {
            double dx = other.getX() - agv.getX();
            double dy = other.getY() - agv.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 0.01) continue;
            if (dist > DETECT_RADIUS) continue;
            double diff = Math.abs(normalizeAngle(Math.toDegrees(Math.atan2(dy, dx)) - agv.getAngle()));
            if (diff <= HALF_FOV_DEG) result.add(new Detection(dist, other.getId()));
        }

        return result;
    }

    private double normalizeAngle(double deg) {
        while (deg >  180) deg -= 360;
        while (deg < -180) deg += 360;
        return deg;
    }
}
