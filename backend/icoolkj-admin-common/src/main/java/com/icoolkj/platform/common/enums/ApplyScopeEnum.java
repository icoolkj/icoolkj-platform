package com.icoolkj.platform.common.enums;

/**
 * {@code sys_ip_access_rule.apply_scope}
 */
public enum ApplyScopeEnum {

    LOGIN("1"),
    ADMIN("2"),
    OPEN_API("3"),
    ALL("4");

    private final String code;

    ApplyScopeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean matchesScope(String applyScope) {
        if (ALL.code.equals(applyScope)) {
            return true;
        }
        return code.equals(applyScope);
    }
}
