package com.icoolkj.platform.system.i18n.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 平台国际化文案表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class I18nMessagePageQuery extends PageQuery {
    private String messageKey;
    private String locale;
    private String messageValue;
    private String module;
    private String scope;
    private String remark;
}
