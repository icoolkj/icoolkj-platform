package com.icoolkj.platform.system.i18n.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 国际化语言配置表 — maps to `sys_i18n_locale`. */
@TableName("sys_i18n_locale")
@Getter
@Setter
public class I18nLocaleEntity extends TenantBaseEntity {

    @TableField("locale_code")
    private String localeCode;

    @TableField("locale_name")
    private String localeName;

    @TableField("native_name")
    private String nativeName;

    @TableField("sort_no")
    private Integer sortNo;

    @TableField("is_default")
    private String isDefault;

    private String status;

    private String remark;

}
