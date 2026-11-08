package com.icoolkj.platform.common.enums;

/**
 * {@code sys_ip_access_rule.match_type}
 */
public enum IpMatchTypeEnum {

    SINGLE("1"),
    RANGE("2"),
    CIDR("3");

    private final String code;

    IpMatchTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
