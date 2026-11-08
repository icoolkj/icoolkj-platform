package com.icoolkj.platform.system.tenant.service;

/**
 * Copies 三权角色模板 (and menus) into a newly created business tenant.
 */
public interface TenantRoleBootstrapService {

    void bootstrapSodRoles(String tenantId);
}
