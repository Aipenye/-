package com.wms.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.wms.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
    IPage pageCC(IPage<Goods> page, @Param(Constants.WRAPPER) Wrapper wrapper);

    @Select("SELECT COALESCE(t.name, '未分类') AS name, " +
            "       COALESCE(SUM(COALESCE(g.count, 0)), 0) AS value " +
            "FROM goods g LEFT JOIN goodstype t ON g.goodsType = t.id " +
            "GROUP BY t.id, t.name " +
            "HAVING value > 0")
    List<Map<String, Object>> countByType();
}
