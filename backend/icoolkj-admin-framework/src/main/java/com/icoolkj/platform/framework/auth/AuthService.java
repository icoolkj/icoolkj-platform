package com.icoolkj.platform.framework.auth;

import com.icoolkj.platform.framework.auth.dto.AuthLoginContext;
import com.icoolkj.platform.framework.auth.dto.AuthLoginRequest;
import com.icoolkj.platform.framework.auth.dto.AuthLoginResponse;
import com.icoolkj.platform.framework.security.model.LoginUser;

/**
 * Authentication business service.
 */
public interface AuthService {

    LoginUser login(AuthLoginRequest request, AuthLoginContext context);

    /** Reloads permissions / status for an existing Redis session (refresh rotation). */
    LoginUser loadSessionUser(String tenantId, String userId, String tenantCode);

    /** Rotates opaque refresh and returns a new token pair. */
    AuthLoginResponse refresh(String refreshToken);
}
