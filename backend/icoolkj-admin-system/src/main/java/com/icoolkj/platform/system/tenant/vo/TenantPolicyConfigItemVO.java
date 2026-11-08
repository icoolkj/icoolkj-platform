package com.icoolkj.platform.system.tenant.vo;

import lombok.Data;

/**
 * One tenant-overridable config entry for the policy catalog.
 */
@Data
public class TenantPolicyConfigItemVO {

    private String configKey;
    private String valueType;
    private String platformDefaultValue;
    private String effectiveValue;
    private String tenantValue;
    private boolean overridden;
    /** 敏感项（如标准初始密码）前端用密码框展示 */
    private boolean sensitive;
}
