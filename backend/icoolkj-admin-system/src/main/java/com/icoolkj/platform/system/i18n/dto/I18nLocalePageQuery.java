package com.icoolkj.platform.system.i18n.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 国际化语言配置表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class I18nLocalePageQuery extends PageQuery {
    private String localeCode;
    private String localeName;
    private String nativeName;
    private Integer sortNo;
    private String isDefault;
    private String status;
    private String remark;
}
