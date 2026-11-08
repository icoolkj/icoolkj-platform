package com.icoolkj.platform.framework.auth.dto;

import lombok.Data;

/**
 * Login token response payload.
 */
@Data
public class AuthLoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expireSeconds;
    private long refreshExpireSeconds;
    /** Password ok but TOTP required — no accessToken until /auth/mfa/verify. */
    private boolean mfaRequired;
    private String mfaTicket;
    /** Access granted but must bind MFA before other APIs. */
    private boolean mustEnrollMfa;

    public AuthLoginResponse() {
    }

    public AuthLoginResponse(String accessToken, String tokenType, long expireSeconds) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expireSeconds = expireSeconds;
    }

    public AuthLoginResponse(String accessToken, String refreshToken, String tokenType,
                             long expireSeconds, long refreshExpireSeconds) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expireSeconds = expireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    public static AuthLoginResponse mfaChallenge(String ticket) {
        AuthLoginResponse response = new AuthLoginResponse();
        response.setMfaRequired(true);
        response.setMfaTicket(ticket);
        return response;
    }
}
