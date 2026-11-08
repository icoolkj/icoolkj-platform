package com.icoolkj.platform.system.i18n.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 平台国际化文案表 — maps to `sys_i18n_message`. */
@TableName("sys_i18n_message")
@Getter
@Setter
public class I18nMessageEntity extends TenantBaseEntity {

    @TableField("message_key")
    private String messageKey;

    private String locale;

    @TableField("message_value")
    private String messageValue;

    private String module;

    private String scope;

    private String remark;

}
