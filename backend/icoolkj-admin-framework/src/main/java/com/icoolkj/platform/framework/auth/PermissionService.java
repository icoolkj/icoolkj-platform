package com.icoolkj.platform.framework.auth;

import java.util.List;

/**
 * Loads RBAC permission codes for authenticated users.
 */
public interface PermissionService {

    /**
     * Loads distinct permission codes for the given user within a tenant.
     */
    List<String> loadPermissionCodes(String tenantId, String userId, boolean superAdmin);
}
