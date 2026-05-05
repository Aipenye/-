package com.wms.agv.logic;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 货架间通道区域锁（单向通行管制）
 * 通道 ID 由调用方根据 AGV 当前位置判断
 */
@Component
public class TrafficManager {

    // zoneId → 占用该通道的 AGV id（-1 表示空闲）
    private final Map<String, Integer> zoneLocks = new ConcurrentHashMap<>();

    /** 尝试进入通道，返回 true 表示获得锁 */
    public boolean tryEnter(String zoneId, int agvId) {
        return zoneLocks.merge(zoneId, agvId, (existing, newVal) ->
                existing == agvId ? agvId : existing) == agvId;
    }

    /** 释放通道锁 */
    public void release(String zoneId, int agvId) {
        zoneLocks.remove(zoneId, agvId);
    }

    /** 查询通道是否被其他 AGV 占用 */
    public boolean isBlockedByOther(String zoneId, int agvId) {
        Integer holder = zoneLocks.get(zoneId);
        return holder != null && holder != agvId;
    }

    /** 释放所有通道锁（重置时调用） */
    public void releaseAll() {
        zoneLocks.clear();
    }

    /**
     * 根据 AGV 当前坐标判断所在通道 ID
     * 作业通道（2.5m 宽）按货架列 x 范围 + 通道 y 范围编码
     * 返回 null 表示不在受管制通道内
     */
    public String resolveZone(double x, double y) {
        // 货架列 x 范围（与 WarehouseMap 保持一致）
        String col = null;
        if      (x >= 2  && x <= 10) col = "A";
        else if (x >= 13 && x <= 21) col = "B";
        else if (x >= 24 && x <= 32) col = "C";
        else if (x >= 48 && x <= 56) col = "D";
        else if (x >= 59 && x <= 67) col = "E";
        else if (x >= 70 && x <= 78) col = "F";
        if (col == null) return null;

        // 作业通道 y 范围（货架行之间）
        String row = null;
        if      (y >= 4   && y <= 6.5)  row = "1-2";
        else if (y >= 8.5 && y <= 11)   row = "2-3";
        else if (y >= 13  && y <= 15.5) row = "3-4";
        else if (y >= 24.5 && y <= 27)  row = "5-6";
        else if (y >= 29  && y <= 31.5) row = "6-7";
        else if (y >= 33.5 && y <= 36)  row = "7-8";
        if (row == null) return null;

        return col + "-" + row;
    }
}
