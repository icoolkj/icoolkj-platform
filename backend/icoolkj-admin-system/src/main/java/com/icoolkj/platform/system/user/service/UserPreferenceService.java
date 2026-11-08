package com.icoolkj.platform.system.user.service;

import com.icoolkj.platform.system.user.dto.UserPreferenceUpdateRequest;
import com.icoolkj.platform.system.auth.vo.AuthMeVO;
import com.icoolkj.platform.system.user.vo.UserPreferenceVO;
import com.icoolkj.platform.framework.security.model.LoginUser;

/** 用户 UI 偏好（语言/主题） */
public interface UserPreferenceService {

    UserPreferenceVO getPreference(String tenantId, String userId);

    UserPreferenceVO updatePreference(String tenantId, String userId, UserPreferenceUpdateRequest request);

    AuthMeVO buildAuthMe(LoginUser loginUser);
}
