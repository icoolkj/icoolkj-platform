package com.icoolkj.platform.framework.auth;

import com.icoolkj.platform.framework.auth.dto.AuthLoginResponse;
import com.icoolkj.platform.framework.jwt.JwtProperties;
import com.icoolkj.platform.framework.jwt.JwtTokenService;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.security.service.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Issues access + opaque refresh pairs after a Redis session already exists.
 */
@Service
public class AuthTokenIssuer {

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    public AuthTokenIssuer(JwtTokenService jwtTokenService,
                           JwtProperties jwtProperties,
                           RefreshTokenService refreshTokenService) {
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenService = refreshTokenService;
    }

    /** Issues a new access token and a freshly bound opaque refresh. */
    public AuthLoginResponse issue(LoginUser loginUser) {
        if (loginUser == null || !StringUtils.hasText(loginUser.getSessionId())) {
            throw new IllegalStateException("sessionId required before issuing tokens");
        }
        String refreshToken = refreshTokenService.issue(loginUser.getSessionId());
        return issueAccess(loginUser, refreshToken);
    }

    /** Issues access only, using an already-rotated refresh token. */
    public AuthLoginResponse issueAccess(LoginUser loginUser, String refreshToken) {
        String accessToken = jwtTokenService.createToken(loginUser);
        AuthLoginResponse response = new AuthLoginResponse(
                accessToken,
                refreshToken,
                normalizeTokenType(jwtProperties.getTokenPrefix()),
                jwtTokenService.getAccessTokenExpireSeconds(),
                Math.max(60L, jwtProperties.getRefreshTokenExpireSeconds())
        );
        response.setMustEnrollMfa(loginUser.isMustEnrollMfa());
        return response;
    }

    private String normalizeTokenType(String tokenPrefix) {
        if (tokenPrefix == null) {
            return "Bearer";
        }
        String value = tokenPrefix.trim();
        return value.isEmpty() ? "Bearer" : value;
    }
}
