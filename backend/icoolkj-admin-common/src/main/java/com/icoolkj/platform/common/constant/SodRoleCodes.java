package com.icoolkj.platform.common.constant;

import java.util.Set;

/**
 * 三权分立角色码（同一用户不可同时持有其中两个及以上）。
 */
public final class SodRoleCodes {

    public static final String SYS_ADMIN = "tenant_sys_admin";
    public static final String SEC_ADMIN = "tenant_sec_admin";
    public static final String AUDIT_ADMIN = "tenant_audit_admin";

    public static final Set<String> ALL = Set.of(SYS_ADMIN, SEC_ADMIN, AUDIT_ADMIN);

    /** Default-tenant template used when bootstrapping new tenants. */
    public static final String TEMPLATE_TENANT_ID = "1000000000000000001";

    private SodRoleCodes() {
    }
}
