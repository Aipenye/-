package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.service.BoxOptimizationService;
import com.wms.service.GoodsService;
import com.wms.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.wms.entity.Record;

import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/record")
public class RecordController {
    @Autowired private RecordService          recordService;
    @Autowired private GoodsService           goodsService;
    @Autowired private BoxOptimizationService boxOptimizationService;

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        String goodstype = (String)param.get("goodstype");
        String storage = (String)param.get("storage");
        String roleId = (String)param.get("roleId");
        String userId = (String)param.get("userId");

        Page page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        QueryWrapper<Record> queryWrapper = new QueryWrapper();
        queryWrapper.apply("a.goods=b.id and b.storage=c.id and b.goodsType=d.id ");

        if("2".equals(roleId)){
            queryWrapper.apply(" a.userId= "+userId);
        }

        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            queryWrapper.like("b.name",name);
        }
        if(StringUtils.isNotBlank(goodstype) && !"null".equals(goodstype)){
            queryWrapper.eq("d.id",goodstype);
        }
        if(StringUtils.isNotBlank(storage) && !"null".equals(storage)){
            queryWrapper.eq("c.id",storage);
        }

        IPage result = recordService.pageCC(page,queryWrapper);
        return Result.success(result.getRecords(),result.getTotal());
    }

    @PostMapping("/save")
    public Result save(@RequestBody Record record){
        Goods goods = goodsService.getById(record.getGoods());
        int n = record.getCount();
        if("2".equals(record.getAction())){
            n = -n;
            record.setCount(n);
        }

        goods.setCount(goods.getCount() + n);
        goodsService.updateById(goods);

        boolean ok = recordService.save(record);
        if (ok && goods.getStorage() != null) {
            // 货物数量变动后异步重新计算装箱布局
            boxOptimizationService.optimizeAndSave(goods.getStorage());
        }
        return ok ? Result.success() : Result.fail();
    }
}

