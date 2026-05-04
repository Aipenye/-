package com.wms.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.wms.entity.Record;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface RecordMapper extends BaseMapper<Record> {
    IPage pageCC(IPage<Record> page, @Param(Constants.WRAPPER) Wrapper wrapper);

    @Select("SELECT DATE(createtime) AS day, SUM(count) AS cnt " +
            "FROM record WHERE count > 0 AND createtime >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(createtime) ORDER BY day")
    List<Map<String, Object>> dailyInCount(@Param("days") int days);

    @Select("SELECT DATE(createtime) AS day, SUM(ABS(count)) AS cnt " +
            "FROM record WHERE count < 0 AND createtime >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(createtime) ORDER BY day")
    List<Map<String, Object>> dailyOutCount(@Param("days") int days);

    @Select("SELECT DATE(createtime) AS day, COUNT(*) AS cnt " +
            "FROM record WHERE createtime >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(createtime) ORDER BY day")
    List<Map<String, Object>> dailyOrderCount(@Param("days") int days);
}
