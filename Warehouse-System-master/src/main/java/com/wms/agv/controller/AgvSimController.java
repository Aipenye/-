package com.wms.agv.controller;

import com.wms.agv.dto.SimStateDTO;
import com.wms.agv.engine.AgvSimulationEngine;
import com.wms.agv.engine.WarehouseMap;
import com.wms.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /** 返回仓库静态地图数据（货架、禁区），前端初始化时调用一次 */
    @GetMapping("/map")
    public Result mapData() {
        return Result.success(Map.of(
            "widthM",    WarehouseMap.WIDTH_M,
            "heightM",   WarehouseMap.HEIGHT_M,
            "cellSize",  WarehouseMap.CELL_SIZE,
            "shelves",   map.getShelfRects(),
            "humanZones",map.getHumanZones()
        ));
    }
}
