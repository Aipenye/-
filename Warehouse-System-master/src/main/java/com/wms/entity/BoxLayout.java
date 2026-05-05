package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("box_layout")
public class BoxLayout implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer storageId;

    private String layoutJson;

    private LocalDateTime updatedAt;
}
