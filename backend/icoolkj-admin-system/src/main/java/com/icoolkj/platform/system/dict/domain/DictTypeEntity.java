package com.icoolkj.platform.system.dict.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 字典类型表 — maps to `sys_dict_type`. */
@TableName("sys_dict_type")
@Getter
@Setter
public class DictTypeEntity extends TenantBaseEntity {

    @TableField("dict_name")
    private String dictName;

    @TableField("dict_type")
    private String dictType;

    private String status;

    private String remark;

}
