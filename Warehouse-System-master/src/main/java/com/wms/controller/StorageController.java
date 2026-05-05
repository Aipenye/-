package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.agv.engine.AgvSimulationEngine;
import com.wms.agv.engine.WarehouseMap;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Storage;
import com.wms.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired private StorageService      storageService;
    @Autowired private WarehouseMap        warehouseMap;
    @Autowired private AgvSimulationEngine agvEngine;

    @PostMapping("/save")
    public Result save(@RequestBody Storage storage) {
        if (storage.getUsed() == null) storage.setUsed(0);
        // 自动分配距出口最近的未注册货架位
        if (storage.getAgvSlot() == null) {
            int slot = warehouseMap.findNearestUnregisteredSlot();
            if (slot > 0) storage.setAgvSlot(slot);
        }
        boolean ok = storageService.save(storage);
        if (ok && storage.getAgvSlot() != null) {
            warehouseMap.registerSlot(storage.getAgvSlot());
        }
        return ok ? Result.success() : Result.fail();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Storage storage) {
        storage.setUsed(null);
        // 若更改了agvSlot，同步地图
        Storage old = storageService.getById(storage.getId());
        boolean ok = storageService.updateById(storage);
        if (ok) {
            if (old != null && old.getAgvSlot() != null) {
                warehouseMap.unregisterSlot(old.getAgvSlot());
            }
            if (storage.getAgvSlot() != null) {
                warehouseMap.registerSlot(storage.getAgvSlot());
            }
        }
        return ok ? Result.success() : Result.fail();
    }

    @GetMapping("/del")
    public Result del(@RequestParam String id) {
        Storage storage = storageService.getById(id);
        boolean ok = storageService.removeById(id);
        if (ok && storage != null && storage.getAgvSlot() != null) {
            warehouseMap.unregisterSlot(storage.getAgvSlot());
        }
        return ok ? Result.success() : Result.fail();
    }

    @GetMapping("/list")
    public Result list() {
        List<Storage> list = storageService.list();
        return Result.success(list);
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        String name = (String) param.get("name");

        Page<Storage> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<Storage> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name) && !"null".equals(name)) {
            queryWrapper.like(Storage::getName, name);
        }

        IPage result = storageService.pageCC(page, queryWrapper);
        return Result.success(result.getRecords(), result.getTotal());
    }
}
