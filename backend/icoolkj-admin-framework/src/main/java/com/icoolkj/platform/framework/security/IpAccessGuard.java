package com.icoolkj.platform.framework.security;

/**
 * Post-login IP access checks for ADMIN/ALL scopes.
 */
public interface IpAccessGuard {

    void ensureAdminAccessAllowed(String tenantId, String clientIp);
}
