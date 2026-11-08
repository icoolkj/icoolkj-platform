package com.icoolkj.platform.system.tenant.service;

/**
 * Tenant lookup for login and administration.
 */
public interface TenantQueryService {

    /**
     * Resolves login {@code tenantCode} to {@code tenant_id}.
     *
     * @param tenantCode client tenant code (e.g. {@code platform}, {@code default})
     * @return resolved tenant id
     */
    String resolveLoginTenantId(String tenantCode);
}
