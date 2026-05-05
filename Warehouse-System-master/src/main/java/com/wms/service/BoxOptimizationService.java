package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.entity.Goods;
import com.wms.entity.Storage;
import com.xlrit.boxoptimization.algorithms.SolvingStrategy;
import com.xlrit.boxoptimization.model.Container;
import com.xlrit.boxoptimization.model.Item;
import com.xlrit.boxoptimization.model.Warehouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BoxOptimizationService {

    @Autowired private Map<String, SolvingStrategy> strategyMap;
    @Autowired private StorageService               storageService;
    @Autowired private GoodsService                 goodsService;
    @Autowired private BoxLayoutService             boxLayoutService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 异步执行装箱优化并将结果持久化。
     * 在工单完成后由 AgvSimulationEngine 调用。
     */
    @Async
    public void optimizeAndSave(Integer storageId) {
        try {
            Storage storage = storageService.getById(storageId);
            if (storage == null) return;

            double sLen = storage.getLength() != null ? storage.getLength() : 500.0;
            double sHei = storage.getHeight() != null ? storage.getHeight() : 300.0;
            double sWid = storage.getWidth()  != null ? storage.getWidth()  : 300.0;
            if (sLen <= 0 || sHei <= 0 || sWid <= 0) return;

            List<Goods> goodsList = goodsService.list(
                    new LambdaQueryWrapper<Goods>().eq(Goods::getStorage, storageId));

            List<Item> items = new ArrayList<>();
            for (Goods goods : goodsList) {
                if (goods.getLength() == null || goods.getHeight() == null || goods.getWidth() == null
                        || goods.getLength() <= 0 || goods.getHeight() <= 0 || goods.getWidth() <= 0) continue;
                int count = goods.getCount() != null ? goods.getCount() : 1;
                for (int i = 0; i < count; i++) {
                    items.add(new Item(goods.getId() + "_" + i,
                            goods.getLength(), goods.getWidth(), goods.getHeight()));
                }
            }
            if (items.isEmpty()) return;

            Container container = new Container("1", sLen, sWid, sHei, items);
            Warehouse warehouse = new Warehouse(sLen, sHei, sWid, List.of(container));

            SolvingStrategy strategy = strategyMap.get("skjolber");
            if (strategy == null) return;

            Warehouse result = strategy.solve(warehouse);
            String json = objectMapper.writeValueAsString(result);
            boxLayoutService.saveOrUpdateLayout(storageId, json);
        } catch (Exception ignored) {}
    }
}
