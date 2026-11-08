package com.icoolkj.platform.common.enums;

/**
 * Operation result status stored in {@code sys_operation_log.operation_status}.
 */
public enum OperationStatusEnum {

    SUCCESS("0"),
    FAIL("1");

    private final String code;

    OperationStatusEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
