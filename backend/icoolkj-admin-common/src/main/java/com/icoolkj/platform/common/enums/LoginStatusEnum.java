package com.icoolkj.platform.common.enums;

/**
 * Login result status stored in {@code sys_login_log.login_status}.
 */
public enum LoginStatusEnum {

    SUCCESS("0"),
    FAIL("1");

    private final String code;

    LoginStatusEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
