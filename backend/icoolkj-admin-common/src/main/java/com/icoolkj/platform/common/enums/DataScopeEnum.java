package com.icoolkj.platform.common.enums;

/**
 * 角色数据权限范围（与 {@code sys_role.data_scope} 编码一致）。
 */
public enum DataScopeEnum {

    ALL("1"),
    DEPT_AND_CHILD("2"),
    DEPT("3"),
    SELF("4"),
    CUSTOM("5");

    private final String code;

    DataScopeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static DataScopeEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return SELF;
        }
        String normalized = code.trim();
        for (DataScopeEnum value : values()) {
            if (value.code.equals(normalized)) {
                return value;
            }
        }
        return SELF;
    }

    public static String normalizeCode(String code) {
        return fromCode(code).getCode();
    }
}
