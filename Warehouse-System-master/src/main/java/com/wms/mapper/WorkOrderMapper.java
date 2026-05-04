package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    @Select("SELECT w.*, g.name AS goodsName, s.name AS storageName, " +
            "a.name AS adminName, u.name AS workerName " +
            "FROM work_order w " +
            "LEFT JOIN goods g ON w.goods_id = g.id " +
            "LEFT JOIN storage s ON w.storage_id = s.id " +
            "LEFT JOIN user a ON w.admin_id = a.id " +
            "LEFT JOIN user u ON w.worker_id = u.id " +
            "WHERE w.id = #{id}")
    WorkOrder selectDetail(@Param("id") Integer id);

    List<WorkOrder> listWithDetail(@Param("workerId") Integer workerId,
                                   @Param("status") Integer status);
}
