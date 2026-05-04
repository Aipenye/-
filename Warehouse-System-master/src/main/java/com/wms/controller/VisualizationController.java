package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.Storage;
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

    @Autowired
    private StorageService storageService;

    @Autowired
    private GoodsService goodsService;

    /**
     * 返回指定仓库的货物数据，格式符合 box-optimization SolveButtonDTO
     * GET /visualization/storage/{storageId}
     */
    @GetMapping("/storage/{storageId}")
    public Result getStorageVisualization(@PathVariable Integer storageId) {
        Storage storage = storageService.getById(storageId);
        if (storage == null) {
            return Result.fail("仓库不存在");
        }

        double sLen = storage.getLength() != null ? storage.getLength() : 500.0;
        double sHei = storage.getHeight() != null ? storage.getHeight() : 300.0;
        double sWid = storage.getWidth()  != null ? storage.getWidth()  : 300.0;

        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStorage, storageId);
        List<Goods> goodsList = goodsService.list(wrapper);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Goods goods : goodsList) {
            double gLen = goods.getLength() != null ? goods.getLength() : 1.0;
            double gHei = goods.getHeight() != null ? goods.getHeight() : 1.0;
            double gWid = goods.getWidth()  != null ? goods.getWidth()  : 1.0;
            int qty = goods.getCount() != null ? goods.getCount() : 1;
            for (int i = 0; i < qty; i++) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", goods.getId() + "_" + i);
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
        warehouse.put("length", sLen);
        warehouse.put("height", sHei);
        warehouse.put("width",  sWid);
        warehouse.put("containers",    Collections.singletonList(container));
        warehouse.put("unplacedItems", Collections.emptyList());

        // 仓库属性信息
        Map<String, Object> storageInfo = new LinkedHashMap<>();
        storageInfo.put("name",   storage.getName() != null ? storage.getName() : "仓库");
        storageInfo.put("length", sLen);
        storageInfo.put("height", sHei);
        storageInfo.put("width",  sWid);

        // 货物列表（用于前端展示，不展开数量）
        List<Map<String, Object>> goodsDisplayList = new ArrayList<>();
        for (Goods goods : goodsList) {
            Map<String, Object> g = new LinkedHashMap<>();
            g.put("id",     goods.getId());
            g.put("name",   goods.getName() != null ? goods.getName() : "未命名");
            g.put("length", goods.getLength() != null ? goods.getLength() : 1.0);
            g.put("height", goods.getHeight() != null ? goods.getHeight() : 1.0);
            g.put("width",  goods.getWidth()  != null ? goods.getWidth()  : 1.0);
            g.put("count",  goods.getCount()  != null ? goods.getCount()  : 1);
            goodsDisplayList.add(g);
        }

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("solvingStrategy", "skjolber");
        dto.put("warehouse",  warehouse);
        dto.put("storageInfo", storageInfo);
        dto.put("goodsList",   goodsDisplayList);

        return Result.success(dto);
    }
}
