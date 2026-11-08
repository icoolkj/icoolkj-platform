package com.icoolkj.platform.common.enums;

import com.icoolkj.platform.common.constant.CommonConstants;

/**
 * 通用业务状态：{@code 0}=正常，{@code 1}=停用（对齐平台多数 sys_* / infra_* status 列）。
 */
public enum StatusEnum {

    NORMAL(CommonConstants.STATUS_NORMAL),
    DISABLE(CommonConstants.STATUS_DISABLE);

    private final String code;

    StatusEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean isNormal(String value) {
        return NORMAL.code.equals(value);
    }

    /** 停用或空（防御）：按停用处理时用 {@link #isDisabled(String)}。 */
    public static boolean isDisabled(String value) {
        return DISABLE.code.equals(value);
    }
}
