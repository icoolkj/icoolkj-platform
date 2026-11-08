package com.icoolkj.platform.system.tenant.dto;

import lombok.Data;

/**
 * 租户开户可选项（均可空，空则走默认约定）。
 */
@Data
public class TenantBootstrapOptions {

    /** 管理员登录名；空则企业 tenant_admin / 单位 org_admin */
    private String adminUsername;

    /** 根部门编码；空则 root */
    private String rootDeptCode;

    /** 根部门名称；空则租户名称 */
    private String rootDeptName;
}
