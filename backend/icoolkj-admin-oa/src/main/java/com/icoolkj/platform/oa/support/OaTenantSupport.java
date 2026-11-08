package com.icoolkj.platform.oa.support;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;

/** OA 平台/租户操作上下文 */
public final class OaTenantSupport {

    private OaTenantSupport() {
    }

    /** 平台模板归属租户（固定 0） */
    public static String platformTenantId() {
        return TenantConstants.PLATFORM_TENANT_ID;
    }

    /** 租户模板归属租户 */
    public static String tenantOwnerId() {
        if (TenantContextHolder.isPlatform()) {
            throw new BusinessException("error.oa.tenant_operator_required");
        }
        String tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BusinessException("error.forbidden");
        }
        return tenantId;
    }

    public static void assertPlatformOperator() {
        if (!TenantContextHolder.isPlatform()) {
            throw new BusinessException("error.oa.platform_operator_required");
        }
    }

    public static void assertTenantOperator() {
        if (TenantContextHolder.isPlatform()) {
            throw new BusinessException("error.oa.tenant_operator_required");
        }
    }
}
