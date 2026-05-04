package com.wms.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.WorkOrder;
import com.wms.mapper.WorkOrderMapper;
import com.wms.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrder> implements WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Override
    public List<WorkOrder> listWithDetail(Integer workerId, Integer status) {
        return workOrderMapper.listWithDetail(workerId, status);
    }

    @Override
    public WorkOrder getDetail(Integer id) {
        return workOrderMapper.selectDetail(id);
    }
}
