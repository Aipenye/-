package com.wms.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.wms.entity.Storage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author linsuwen
 * @since 2023-01-05
 */
@Mapper
public interface StorageMapper extends BaseMapper<Storage> {
    IPage pageCC(IPage<Storage> page, @Param(Constants.WRAPPER) Wrapper wrapper);

    /** 列出所有仓库，并聚合每个仓库内的货物总体积（cm³）到 usedVolume 字段 */
    List<Storage> listAllWithUsedVolume();
}
