package com.icoolkj.platform.common.enums;

import org.springframework.util.StringUtils;

/**
 * File malware scan status stored in {@code infra_file.scan_status}.
 */
public enum FileScanStatusEnum {

    PENDING("0"),
    CLEAN("1"),
    INFECTED("2"),
    SKIPPED("3");

    private final String code;

    FileScanStatusEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static FileScanStatusEnum fromCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        for (FileScanStatusEnum value : values()) {
            if (value.code.equals(code.trim())) {
                return value;
            }
        }
        return null;
    }
}
