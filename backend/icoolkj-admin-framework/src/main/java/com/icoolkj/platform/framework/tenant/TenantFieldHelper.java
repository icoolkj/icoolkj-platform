package com.icoolkj.platform.framework.tenant;

import org.springframework.util.StringUtils;

/**
 * Resolves {@code tenant_id} for manual MyBatis inserts when the request omits it.
 * Update flows must not overwrite existing tenant_id with null.
 */
public final class TenantFieldHelper {

    private TenantFieldHelper() {
    }

    public static String resolveTenantId(String requestTenantId) {
        if (StringUtils.hasText(requestTenantId)) {
            return requestTenantId;
        }
        if (TenantContextHolder.hasTenant()) {
            return TenantContextHolder.getTenantId();
        }
        return null;
    }
}
