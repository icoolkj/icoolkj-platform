package com.icoolkj.platform.system.config.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 系统参数表 — maps to `sys_config`. */
@TableName("sys_config")
@Getter
@Setter
public class ConfigEntity extends TenantBaseEntity {

    @TableField("config_name")
    private String configName;

    @TableField("config_key")
    private String configKey;

    @TableField("config_value")
    private String configValue;

    @TableField("config_type")
    private String configType;

    private String remark;

}
