package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.WorkOrder;

import java.util.List;

public interface WorkOrderService extends IService<WorkOrder> {

    List<WorkOrder> listWithDetail(Integer workerId, Integer status);

    WorkOrder getDetail(Integer id);
}
