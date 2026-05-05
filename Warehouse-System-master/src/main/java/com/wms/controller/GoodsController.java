package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.service.BoxOptimizationService;
import com.wms.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Autowired private GoodsService           goodsService;
    @Autowired private BoxOptimizationService boxOptimizationService;

    @GetMapping("/list")
    public Result list(){
        return Result.success(goodsService.list());
    }

    @PostMapping("/save")
    public Result save(@RequestBody Goods goods){
        boolean ok = goodsService.save(goods);
        if (ok && goods.getStorage() != null) {
            boxOptimizationService.optimizeAndSave(goods.getStorage());
        }
        return ok ? Result.success() : Result.fail();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Goods goods){
        // 取旧记录，确保能拿到 storageId（前端可能只传部分字段）
        Goods old = goodsService.getById(goods.getId());
        boolean ok = goodsService.updateById(goods);
        if (ok) {
            Integer storageId = goods.getStorage() != null ? goods.getStorage()
                    : (old != null ? old.getStorage() : null);
            if (storageId != null) {
                boxOptimizationService.optimizeAndSave(storageId);
            }
            // 若货物被移到了另一个仓库，旧仓库也需要重算
            if (old != null && old.getStorage() != null
                    && !old.getStorage().equals(storageId)) {
                boxOptimizationService.optimizeAndSave(old.getStorage());
            }
        }
        return ok ? Result.success() : Result.fail();
    }

    @GetMapping("/del")
    public Result del(@RequestParam String id){
        Goods goods = goodsService.getById(id);
        boolean ok = goodsService.removeById(id);
        if (ok && goods != null && goods.getStorage() != null) {
            boxOptimizationService.optimizeAndSave(goods.getStorage());
        }
        return ok ? Result.success() : Result.fail();
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        String goodstype = (String)param.get("goodstype");
        String storage = (String)param.get("storage");

        Page<Goods> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            queryWrapper.like(Goods::getName,name);
        }
        if(StringUtils.isNotBlank(goodstype) && !"null".equals(goodstype)){
            queryWrapper.like(Goods::getGoodstype,goodstype);
        }
        if(StringUtils.isNotBlank(storage) && !"null".equals(storage)){
            queryWrapper.like(Goods::getStorage,storage);
        }

        IPage result = goodsService.pageCC(page,queryWrapper);
        return Result.success(result.getRecords(),result.getTotal());
    }
}

