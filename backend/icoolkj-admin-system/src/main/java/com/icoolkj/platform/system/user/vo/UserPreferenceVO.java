package com.icoolkj.platform.system.user.vo;

import lombok.Data;

/** 当前用户 UI 偏好 */
@Data
public class UserPreferenceVO {

    private String locale;
    private String theme;
    private LayoutPreferenceVO layout;

}

