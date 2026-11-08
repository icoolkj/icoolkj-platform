package com.icoolkj.platform.framework.tenant;

import com.icoolkj.platform.common.constant.TenantConstants;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Runtime tenant identity for the current request.
 * <p>
 * Resolution belongs in {@link TenantWebInterceptor}; thread binding in {@link TenantContextHolder}.
 */
@Data
public class TenantContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String tenantId;
    private String tenantCode;
    private boolean platform;
    private boolean ignoreTenant;

    public TenantContext() {
    }

    public TenantContext(String tenantId, String tenantCode, boolean platform, boolean ignoreTenant) {
        this.tenantId = tenantId;
        this.tenantCode = tenantCode;
        this.platform = platform;
        this.ignoreTenant = ignoreTenant;
    }

    public static TenantContext tenant(String tenantId, String tenantCode) {
        return new TenantContext(tenantId, tenantCode, false, false);
    }

    public static TenantContext platform(String tenantCode) {
        return new TenantContext(TenantConstants.PLATFORM_TENANT_ID, tenantCode, true, false);
    }

    public static TenantContext platform() {
        return platform("platform");
    }

    public static TenantContext ignore() {
        return new TenantContext(null, null, false, true);
    }
}
