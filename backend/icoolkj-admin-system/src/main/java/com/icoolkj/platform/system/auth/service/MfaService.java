package com.icoolkj.platform.system.auth.service;

import com.icoolkj.platform.framework.auth.dto.AuthLoginResponse;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.auth.dto.MfaCodeRequest;
import com.icoolkj.platform.system.auth.dto.MfaVerifyRequest;
import com.icoolkj.platform.system.auth.vo.MfaEnrollStartVO;

public interface MfaService {

    /** Whether password-ok login should challenge TOTP instead of issuing JWT. */
    boolean requiresMfaChallenge(LoginUser loginUser);

    String createPreAuthTicket(LoginUser loginUser, String tenantCode);

    AuthLoginResponse verifyLogin(MfaVerifyRequest request);

    boolean shouldForceEnroll(LoginUser loginUser);

    MfaEnrollStartVO startEnroll(LoginUser loginUser);

    void confirmEnroll(LoginUser loginUser, MfaCodeRequest request);

    void disable(LoginUser loginUser, MfaCodeRequest request);

    boolean isUserMfaEnabled(String userId);
}
