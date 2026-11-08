package com.icoolkj.platform.framework.security.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Server-side login session stored in Redis.
 */
@Data
public class LoginSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String userId;
    private String username;
    private String tenantId;
    private String tenantCode;
    private Instant createdAt;
    /** When true, only password-change / me / logout APIs are allowed. */
    private boolean mustChangePassword;
    /** When true, only MFA enroll / me / logout (and password if also forced) APIs are allowed. */
    private boolean mustEnrollMfa;
    /** SHA-256 hex of the current opaque refresh token (server-side only). */
    private String refreshTokenHash;
}
