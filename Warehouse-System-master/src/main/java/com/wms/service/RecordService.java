package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.Record;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface RecordService extends IService<Record> {
    IPage pageCC(IPage<Record> page, Wrapper wrapper);
    List<Map<String, Object>> dailyInCount(int days);
    List<Map<String, Object>> dailyOutCount(int days);
    List<Map<String, Object>> dailyOrderCount(int days);
}
