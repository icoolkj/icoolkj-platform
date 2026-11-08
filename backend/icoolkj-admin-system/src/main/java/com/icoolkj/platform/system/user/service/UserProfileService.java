package com.icoolkj.platform.system.user.service;

import com.icoolkj.platform.system.auth.dto.ChangePasswordRequest;
import com.icoolkj.platform.system.user.dto.UserAvatarBindRequest;
import com.icoolkj.platform.system.user.dto.UserProfileUpdateRequest;
import com.icoolkj.platform.system.user.vo.UserProfileVO;

/** 当前登录用户个人资料 */
public interface UserProfileService {

    UserProfileVO getProfile(String tenantId, String tenantCode, String userId);

    UserProfileVO updateProfile(String tenantId, String tenantCode, String userId, UserProfileUpdateRequest request);

    UserProfileVO applyAvatar(String tenantId, String tenantCode, String userId, UserAvatarBindRequest request);

    void changePassword(String tenantId, String userId, ChangePasswordRequest request);
}
