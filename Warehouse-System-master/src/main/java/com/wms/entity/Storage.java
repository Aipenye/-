package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Storage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private String remark;

    private Integer capacity;

    private Integer used;

    private Double length;

    private Double height;

    private Double width;

    /** AGV货架编号(1-48)，对应仓库地图中的货架格子，NULL表示未注册 */
    private Integer agvSlot;

    /** 该仓库内货物总体积(cm³)，由SQL聚合计算，非数据库列 */
    @TableField(exist = false)
    private Double usedVolume;
}
