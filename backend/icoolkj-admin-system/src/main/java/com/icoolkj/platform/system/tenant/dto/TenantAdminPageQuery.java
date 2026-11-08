package com.icoolkj.platform.system.tenant.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 平台：租户管理员分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantAdminPageQuery extends PageQuery {

    private String tenantCode;
    private String tenantName;
    private String username;
    private String status;
}
