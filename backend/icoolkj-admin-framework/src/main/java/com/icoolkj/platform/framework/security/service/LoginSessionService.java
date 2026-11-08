package com.icoolkj.platform.framework.security.service;

import com.icoolkj.platform.framework.security.model.LoginSession;
import com.icoolkj.platform.framework.security.model.LoginUser;

import java.util.List;
import java.util.Optional;

/**
 * Redis-backed login session lifecycle.
 */
public interface LoginSessionService {

    /**
     * Creates a session and stores it in Redis.
     *
     * @param options concurrent-login policy for this tenant (from sys_config / YAML)
     * @return generated session id (sid)
     */
    String createSession(LoginUser loginUser, String tenantCode, ConcurrentLoginOptions options);

    Optional<LoginSession> getSession(String sessionId);

    boolean isSessionValid(String sessionId, String userId);

    void removeSession(String sessionId);

    /**
     * Removes all Redis login sessions for the user (e.g. after password or permission change).
     */
    void removeAllUserSessions(String tenantId, String userId);

    /** Active sessions for a user (newest first). */
    List<LoginSession> listUserSessions(String tenantId, String userId);

    /** Clears the must-change-password gate on the current session after a successful password change. */
    void clearMustChangePassword(String sessionId);

    /** Clears the must-enroll-MFA gate after successful TOTP binding. */
    void clearMustEnrollMfa(String sessionId);

    /**
     * Scans Redis for online sessions.
     *
     * @param tenantId when non-blank, only that tenant; platform callers may pass null/blank for all
     */
    List<LoginSession> listOnlineSessions(String tenantId);

    /** Persists session JSON and refreshes TTL (e.g. after refresh-token bind / flag clear). */
    void saveSession(LoginSession session);
}
