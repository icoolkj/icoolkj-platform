package com.icoolkj.platform.common.enums;

/**
 * {@code sys_ip_access_rule.rule_type}
 */
public enum IpRuleTypeEnum {

    ALLOW("1"),
    DENY("2");

    private final String code;

    IpRuleTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean isAllow() {
        return this == ALLOW;
    }

    public boolean isDeny() {
        return this == DENY;
    }
}
