package com.icoolkj.platform.common.enums;

import org.springframework.util.StringUtils;

/**
 * File sensitivity level stored in {@code infra_file.sensitivity_level}.
 */
public enum FileSensitivityEnum {

    PUBLIC("0"),
    INTERNAL("1"),
    SECRET("2"),
    CONFIDENTIAL("3");

    private final String code;

    FileSensitivityEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static FileSensitivityEnum fromCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        for (FileSensitivityEnum value : values()) {
            if (value.code.equals(code.trim())) {
                return value;
            }
        }
        return null;
    }

    /** Whether {@code code} is at least as sensitive as {@code min}. */
    public static boolean isAtLeast(String code, FileSensitivityEnum min) {
        if (min == null) {
            return false;
        }
        FileSensitivityEnum current = fromCode(code);
        if (current == null) {
            return false;
        }
        return current.ordinal() >= min.ordinal();
    }
}
