package com.icoolkj.platform.common.constant;

/** {@code sys_tenant.tenant_type} */
public final class TenantType {

    public static final String ENTERPRISE = "ENTERPRISE";
    public static final String ORGANIZATION = "ORGANIZATION";

    private TenantType() {
    }

    public static boolean isEnterprise(String tenantType) {
        return ENTERPRISE.equals(tenantType);
    }

    public static boolean isOrganization(String tenantType) {
        return ORGANIZATION.equals(tenantType);
    }
}
