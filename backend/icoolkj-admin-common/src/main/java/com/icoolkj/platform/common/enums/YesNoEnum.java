package com.icoolkj.platform.common.enums;

import com.icoolkj.platform.common.constant.CommonConstants;
import org.springframework.util.StringUtils;

/**
 * DB {@code char(1)} 是否标志：{@code 1}=是，{@code 0}=否。
 */
public enum YesNoEnum {

    YES(CommonConstants.YES),
    NO(CommonConstants.NO);

    private final String code;

    YesNoEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean isYes(String value) {
        return YES.code.equals(value);
    }

    public static boolean isNo(String value) {
        return NO.code.equals(value);
    }

    /** 解析常见真值：{@code 1}/{@code Y}/{@code true}（忽略大小写）。 */
    public static boolean parseTruthy(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String raw = value.trim();
        return isYes(raw) || "Y".equalsIgnoreCase(raw) || "true".equalsIgnoreCase(raw);
    }
}
