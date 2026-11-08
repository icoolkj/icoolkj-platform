package com.icoolkj.platform.system.dict.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 字典数据项表 — maps to `sys_dict_item`. */
@TableName("sys_dict_item")
@Getter
@Setter
public class DictItemEntity extends TenantBaseEntity {

    @TableField("dict_type_id")
    private String dictTypeId;

    @TableField("dict_label")
    private String dictLabel;

    @TableField("dict_value")
    private String dictValue;

    @TableField("value_type")
    private String valueType;

    @TableField("css_class")
    private String cssClass;

    private String icon;

    @TableField("list_class")
    private String listClass;

    @TableField("is_default")
    private String isDefault;

    @TableField("sort_no")
    private Integer sortNo;

    private String status;

    private String remark;

}
