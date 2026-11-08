package com.icoolkj.platform.infra.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 第三方调用日志分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ThirdPartyCallLogPageQuery extends PageQuery {

    private String tenantId;
    private String providerName;
    private String responseStatus;
}
