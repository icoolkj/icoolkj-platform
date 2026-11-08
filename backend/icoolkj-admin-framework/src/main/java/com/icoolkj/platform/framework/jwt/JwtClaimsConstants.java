package com.icoolkj.platform.framework.jwt;

/**
 * JWT custom claim names.
 */
public final class JwtClaimsConstants {

    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String NICKNAME = "nickname";
    public static final String TENANT_ID = "tenantId";
    public static final String TENANT_CODE = "tenantCode";
    public static final String SESSION_ID = "sid";
    public static final String PERMISSIONS = "permissions";
    public static final String TOKEN_TYPE = "typ";
    public static final String ACCESS_TOKEN_TYPE = "access";

    private JwtClaimsConstants() {
    }
}
