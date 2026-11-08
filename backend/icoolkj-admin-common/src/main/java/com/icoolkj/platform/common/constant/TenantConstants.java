package com.icoolkj.platform.common.constant;

/**
 * Tenant-related constants.
 */
public final class TenantConstants {

    /**
     * Platform-level tenant identifier.
     */
    public static final String PLATFORM_TENANT_ID = "0";

    /**
     * Sentinel tenant id when context is missing under tenant isolation.
     */
    public static final String MISSING_TENANT_ID = "-1";

    private TenantConstants() {
    }
}
