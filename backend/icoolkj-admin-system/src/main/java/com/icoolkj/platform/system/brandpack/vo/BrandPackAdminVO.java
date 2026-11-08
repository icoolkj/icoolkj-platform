package com.icoolkj.platform.system.brandpack.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 管理端 OEM 品牌包（含作用域元数据） */
@Data
@EqualsAndHashCode(callSuper = true)
public class BrandPackAdminVO extends BrandPackVO {

    /** 当前编辑的目标租户 id */
    private String targetTenantId;
    private String targetTenantCode;
    private String targetTenantName;
    /** 业务租户是否已有覆盖行（非平台） */
    private boolean tenantOverride;
    /** 当前登录用户是否可选择其他租户编辑 */
    private boolean canSelectTenant;
}
