package com.wms.agv.controller;

import com.wms.agv.dto.SimStateDTO;
import com.wms.agv.engine.AgvSimulationEngine;
import com.wms.agv.engine.WarehouseMap;
import com.wms.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agv")
public class AgvSimController {

    @Autowired private AgvSimulationEngine engine;
    @Autowired private WarehouseMap        map;

    @PostMapping("/start")
    public Result start() {
        engine.start();
        return Result.success("仿真已启动");
    }

    @PostMapping("/stop")
    public Result stop() {
        engine.stop();
        return Result.success("仿真已暂停");
    }

    @PostMapping("/reset")
    public Result reset() {
        engine.reset();
        return Result.success("仿真已重置");
    }

    @GetMapping("/status")
    public Result status() {
        SimStateDTO snapshot = engine.getSnapshot();
        return Result.success(Map.of(
            "running",  engine.isRunning(),
            "snapshot", snapshot
        ));
    }

    /** 返回仓库静态地图数据，包含货架注册状态、出口、停车区 */
    @GetMapping("/map")
    public Result mapData() {
        // 构建货架信息列表（含编号和注册状态）
        List<Map<String, Object>> shelfInfoList = new ArrayList<>();
        map.getSlotRectMap().forEach((slot, rect) -> {
            shelfInfoList.add(Map.of(
                "slot",       slot,
                "rect",       rect,
                "registered", map.isRegistered(slot)
            ));
        });

        return Result.success(Map.ofEntries(
            Map.entry("widthM",          WarehouseMap.WIDTH_M),
            Map.entry("heightM",         WarehouseMap.HEIGHT_M),
            Map.entry("cellSize",        WarehouseMap.CELL_SIZE),
            Map.entry("shelves",         map.getShelfRects()),
            Map.entry("humanZones",      map.getHumanZones()),
            Map.entry("shelfInfos",      shelfInfoList),
            Map.entry("exitX1",          WarehouseMap.EXIT_X1),
            Map.entry("exitX2",          WarehouseMap.EXIT_X2),
            Map.entry("exitY",           WarehouseMap.EXIT_Y),
            Map.entry("leftParkX1",      WarehouseMap.LEFT_PARK_X1),
            Map.entry("leftParkX2",      WarehouseMap.LEFT_PARK_X2),
            Map.entry("rightParkX1",     WarehouseMap.RIGHT_PARK_X1),
            Map.entry("rightParkX2",     WarehouseMap.RIGHT_PARK_X2),
            Map.entry("parkY1",          WarehouseMap.PARK_Y1),
            Map.entry("parkY2",          WarehouseMap.PARK_Y2),
            Map.entry("parkSlots",       WarehouseMap.PARK_SLOTS),
            Map.entry("parkExitPoints",  WarehouseMap.PARK_EXIT_POINTS)
        ));
    }

    /** 同步货架注册状态（仓库增删后调用） */
    @PostMapping("/syncSlots")
    public Result syncSlots() {
        engine.syncStorageSlots();
        return Result.success("货架状态已同步");
    }

    /** 查询空闲AGV数量 */
    @GetMapping("/idleCount")
    public Result idleCount() {
        return Result.success(engine.countIdleAgvs());
    }
}
