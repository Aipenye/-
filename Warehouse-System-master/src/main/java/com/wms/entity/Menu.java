package com.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    @TableField("menuCode")
    private String menucode;

    @TableField("menuName")
    private String menuname;

    @TableField("menuLevel")
    private String menulevel;

    @TableField("menuParentCode")
    private String menuparentcode;

    @TableField("menuClick")
    private String menuclick;

    @TableField("menuRight")
    private String menuright;

    @TableField("menuComponent")
    private String menucomponent;

    @TableField("menuIcon")
    private String menuicon;


}
