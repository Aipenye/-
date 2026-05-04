package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.Storage;
import com.wms.service.GoodsService;
import com.wms.service.StorageService;
import com.xlrit.boxoptimization.dto.SolveButtonDTO;
import com.xlrit.boxoptimization.model.Container;
import com.xlrit.boxoptimization.model.Item;
import com.xlrit.boxoptimization.model.Warehouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/optimization")
public class OptimizationController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/goodsForOptimize")
    public Result goodsForOptimize(@RequestParam Integer storageId) {
        Storage storage = storageService.getById(storageId);
        if (storage == null) {
            return Result.fail("仓库不存在");
        }
        if (storage.getLength() == null || storage.getHeight() == null || storage.getWidth() == null
                || storage.getLength() <= 0 || storage.getHeight() <= 0 || storage.getWidth() <= 0) {
            return Result.fail("仓库尺寸未设置，请先编辑仓库填写长高宽");
        }

        List<Goods> goodsList = goodsService.list(
                new LambdaQueryWrapper<Goods>().eq(Goods::getStorage, storageId));

        List<Item> items = new ArrayList<>();
        int itemIndex = 0;
        for (Goods goods : goodsList) {
            if (goods.getLength() == null || goods.getHeight() == null || goods.getWidth() == null
                    || goods.getLength() <= 0 || goods.getHeight() <= 0 || goods.getWidth() <= 0) {
                continue;
            }
            int count = goods.getCount() != null ? goods.getCount() : 1;
            for (int i = 0; i < count; i++) {
                items.add(new Item(
                        goods.getId() + "_" + i,
                        goods.getLength(),
                        goods.getWidth(),
                        goods.getHeight()));
                itemIndex++;
            }
        }

        if (items.isEmpty()) {
            return Result.fail("该仓库下没有设置尺寸的货物");
        }

        Container container = new Container(
                "container_1",
                storage.getLength(),
                storage.getWidth(),
                storage.getHeight(),
                items);

        List<Container> containers = new ArrayList<>();
        containers.add(container);

        Warehouse warehouse = new Warehouse(
                storage.getLength(),
                storage.getHeight(),
                storage.getWidth(),
                containers);

        SolveButtonDTO dto = new SolveButtonDTO(warehouse, "skjolber");
        return Result.success(dto);
    }
}
