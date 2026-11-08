package com.icoolkj.platform.system.auth.service;

import com.icoolkj.platform.framework.auth.dto.AuthLoginContext;
import com.icoolkj.platform.system.user.domain.UserEntity;

/**
 * Login failure counting, account/IP lock, and success cleanup.
 */
public interface LoginSecurityPolicyService {

    void ensureLoginAllowed(String tenantId, String username, UserEntity user);

    /**
     * Rejects when the client IP has exceeded {@code security.login.max-fail-count-per-ip}.
     */
    void ensureIpRateAllowed(String tenantId, String clientIp);

    void onLoginSuccess(UserEntity user, AuthLoginContext context);

    void onLoginFailure(String tenantId, String username, UserEntity user, String message, AuthLoginContext context);

    /** Clears Redis login-fail counters for the account (admin unlock). */
    void clearLoginFailCounts(String tenantId, String username);
}
