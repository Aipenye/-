package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class WorkOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer goodsId;

    private Integer storageId;

    private Integer count;

    private LocalDateTime deadline;

    private String remark;

    private Integer adminId;

    private Integer workerId;

    private Integer status;

    private Integer type;

    private LocalDateTime createTime;

    private LocalDateTime finishTime;

    @TableField(exist = false)
    private String goodsName;

    @TableField(exist = false)
    private String storageName;

    @TableField(exist = false)
    private String adminName;

    @TableField(exist = false)
    private String workerName;
}
