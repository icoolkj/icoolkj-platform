package com.icoolkj.platform.common.constant;

/**
 * Redis cache key fragments for platform services.
 */
public final class CacheKeyConstants {

    public static final String AUTH_LOGIN_SESSION = "auth:login:session:";
    public static final String AUTH_USER_SESSIONS = "auth:user:sessions:";
    /** Opaque refresh token hash → sessionId. */
    public static final String AUTH_REFRESH = "auth:refresh:";
    /** Per-tenant audit integrity chain tip (row hash). */
    public static final String AUDIT_CHAIN_TIP = "audit:chain:";
    public static final String AUTH_PERMISSION = "auth:perm:";
    public static final String AUTH_BLACKLIST = "auth:blacklist:";
    public static final String AUTH_LOGIN_FAIL = "auth:login:fail:";
    public static final String AUTH_LOGIN_FAIL_IP = "auth:login:fail:ip:";
    public static final String AUTH_CAPTCHA = "auth:captcha:";
    /** MFA pre-auth ticket after password success (before TOTP). */
    public static final String AUTH_MFA_TICKET = "auth:mfa:ticket:";
    /** Pending MFA enroll secret (not yet confirmed). */
    public static final String AUTH_MFA_ENROLL = "auth:mfa:enroll:";

    public static final String TENANT_CONFIG = "tenant:config:";
    public static final String TENANT_IP_RULE = "tenant:ip-rule:";

    public static final String I18N_MESSAGES = "i18n:messages:";
    public static final String I18N_LOCALES = "i18n:locales";

    /** Per-user file upload rate window (tenantId:userId). */
    public static final String FILE_UPLOAD_RATE = "file:upload:rate:";

    private CacheKeyConstants() {
    }
}
