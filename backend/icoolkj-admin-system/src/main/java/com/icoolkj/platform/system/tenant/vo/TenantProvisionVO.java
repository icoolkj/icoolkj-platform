package com.icoolkj.platform.system.tenant.vo;

import lombok.Data;

/**
 * 租户开通回执：明文初始密码仅在创建响应中出现一次。
 */
@Data
public class TenantProvisionVO {

    private String tenantId;
    private String tenantCode;
    private String tenantName;
    private String adminUsername;
    private String initialPassword;
    private String rootDeptCode;
    private String rootDeptName;
    /** 恒为 true，供前端文案 */
    private boolean mustChangePassword;
}
