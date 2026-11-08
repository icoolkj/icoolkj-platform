package com.icoolkj.platform.system.user.dto;

import com.icoolkj.platform.system.user.vo.LayoutPreferenceVO;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 更新当前用户 UI 偏好 */
@Data
public class UserPreferenceUpdateRequest {

    @Pattern(regexp = "^(zh-CN|en-US)?$", message = "{validation.locale.invalid}")
    private String locale;

    @Pattern(regexp = "^(blue|green|purple|orange|rose)?$", message = "{validation.theme.invalid}")
    private String theme;

    /** Partial layout patch; null fields are ignored. */
    private LayoutPreferenceVO layout;
}
