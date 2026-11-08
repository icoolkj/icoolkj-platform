package com.icoolkj.platform.common.enums;

import com.icoolkj.platform.common.constant.CommonConstants;

/**
 * 用户性别：{@code 0}=男，{@code 1}=女，{@code 2}=未知（对齐字典 {@code sys_gender}）。
 */
public enum GenderEnum {

    MALE(CommonConstants.GENDER_MALE),
    FEMALE(CommonConstants.GENDER_FEMALE),
    UNKNOWN(CommonConstants.GENDER_UNKNOWN);

    private final String code;

    GenderEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean isMale(String value) {
        return MALE.code.equals(value);
    }

    public static boolean isFemale(String value) {
        return FEMALE.code.equals(value);
    }

    public static boolean isUnknown(String value) {
        return UNKNOWN.code.equals(value);
    }

    /** 空或非法码按未知处理。 */
    public static String normalize(String value) {
        if (isMale(value) || isFemale(value) || isUnknown(value)) {
            return value;
        }
        return UNKNOWN.code;
    }
}
