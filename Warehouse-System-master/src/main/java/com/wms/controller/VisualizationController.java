package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.common.Result;
import com.wms.entity.BoxLayout;
import com.wms.entity.Goods;
import com.wms.entity.Storage;
import com.wms.service.BoxLayoutService;
import com.wms.service.GoodsService;
import com.wms.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 为 box-optimization 提供仓库可视化数据接口
 */
@RestController
@RequestMapping("/visualization")
public class VisualizationController {

    @Autowired private StorageService  storageService;
    @Autowired private GoodsService    goodsService;
    @Autowired private BoxLayoutService boxLayoutService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 返回指定仓库的可视化数据。
     * 若已有预计算的装箱布局，直接返回含坐标的结果（precomputed=true）；
     * 否则返回原始货物数据供前端重新计算。
     * GET /visualization/storage/{storageId}
     */
    @GetMapping("/storage/{storageId}")
    public Result getStorageVisualization(@PathVariable Integer storageId) {
        Storage storage = storageService.getById(storageId);
        if (storage == null) return Result.fail("仓库不存在");

        double sLen = storage.getLength() != null ? storage.getLength() : 500.0;
        double sHei = storage.getHeight() != null ? storage.getHeight() : 300.0;
        double sWid = storage.getWidth()  != null ? storage.getWidth()  : 300.0;

        List<Goods> goodsList = goodsService.list(
                new LambdaQueryWrapper<Goods>().eq(Goods::getStorage, storageId));

        // 仓库属性
        Map<String, Object> storageInfo = new LinkedHashMap<>();
        storageInfo.put("name",   storage.getName() != null ? storage.getName() : "仓库");
        storageInfo.put("length", sLen);
        storageInfo.put("height", sHei);
        storageInfo.put("width",  sWid);

        // 货物展示列表（不展开数量）
        List<Map<String, Object>> goodsDisplayList = new ArrayList<>();
        for (Goods g : goodsList) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",     g.getId());
            m.put("name",   g.getName() != null ? g.getName() : "未命名");
            m.put("length", g.getLength() != null ? g.getLength() : 1.0);
            m.put("height", g.getHeight() != null ? g.getHeight() : 1.0);
            m.put("width",  g.getWidth()  != null ? g.getWidth()  : 1.0);
            m.put("count",  g.getCount()  != null ? g.getCount()  : 1);
            goodsDisplayList.add(m);
        }

        // 检查是否有预计算的装箱布局
        BoxLayout cached = boxLayoutService.getByStorageId(storageId);
        if (cached != null && cached.getLayoutJson() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> preResult = objectMapper.readValue(cached.getLayoutJson(), Map.class);
                Map<String, Object> dto = new LinkedHashMap<>();
                dto.put("solvingStrategy", "skjolber");
                dto.put("warehouse",       preResult);
                dto.put("storageInfo",     storageInfo);
                dto.put("goodsList",       goodsDisplayList);
                dto.put("precomputed",     true);
                return Result.success(dto);
            } catch (Exception ignored) {}
        }

        // 无缓存：返回原始数据，前端自行计算
        List<Map<String, Object>> items = new ArrayList<>();
        for (Goods g : goodsList) {
            double gLen = g.getLength() != null ? g.getLength() : 1.0;
            double gHei = g.getHeight() != null ? g.getHeight() : 1.0;
            double gWid = g.getWidth()  != null ? g.getWidth()  : 1.0;
            int qty = g.getCount() != null ? g.getCount() : 1;
            for (int i = 0; i < qty; i++) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id",     g.getId() + "_" + i);
                item.put("length", gLen);
                item.put("height", gHei);
                item.put("width",  gWid);
                item.put("placed", false);
                items.add(item);
            }
        }

        Map<String, Object> container = new LinkedHashMap<>();
        container.put("id", "1");
        container.put("length", sLen);
        container.put("height", sHei);
        container.put("width",  sWid);
        container.put("items",  items);

        Map<String, Object> warehouse = new LinkedHashMap<>();
        warehouse.put("length",       sLen);
        warehouse.put("height",       sHei);
        warehouse.put("width",        sWid);
        warehouse.put("containers",   Collections.singletonList(container));
        warehouse.put("unplacedItems", Collections.emptyList());

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("solvingStrategy", "skjolber");
        dto.put("warehouse",       warehouse);
        dto.put("storageInfo",     storageInfo);
        dto.put("goodsList",       goodsDisplayList);
        dto.put("precomputed",     false);
        return Result.success(dto);
    }
}

