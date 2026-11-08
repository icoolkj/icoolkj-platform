package com.icoolkj.platform.api.controller.auth;

import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.framework.annotation.Idempotent;
import com.icoolkj.platform.framework.annotation.OperationLog;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.infra.service.UserAvatarFileService;
import com.icoolkj.platform.infra.vo.UserAvatarPolicyVO;
import com.icoolkj.platform.infra.vo.UserAvatarUploadVO;
import com.icoolkj.platform.system.auth.dto.ChangePasswordRequest;
import com.icoolkj.platform.system.user.dto.UserAvatarBindRequest;
import com.icoolkj.platform.system.user.dto.UserPreferenceUpdateRequest;
import com.icoolkj.platform.system.user.dto.UserProfileUpdateRequest;
import com.icoolkj.platform.system.user.service.UserPreferenceService;
import com.icoolkj.platform.system.user.service.UserProfileService;
import com.icoolkj.platform.system.user.vo.UserPreferenceVO;
import com.icoolkj.platform.system.user.vo.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 当前登录用户个人中心（资料 / 偏好 / 密码 / 头像） */
@RestController
@RequestMapping("/auth")
public class ProfileController {

    private final UserPreferenceService userPreferenceService;
    private final UserProfileService userProfileService;
    private final UserAvatarFileService userAvatarFileService;

    public ProfileController(UserPreferenceService userPreferenceService,
                             UserProfileService userProfileService,
                             UserAvatarFileService userAvatarFileService) {
        this.userPreferenceService = userPreferenceService;
        this.userProfileService = userProfileService;
        this.userAvatarFileService = userAvatarFileService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileVO> getProfile() {
        LoginUser loginUser = requireLoginUser();
        return ApiResponse.ok(userProfileService.getProfile(
                loginUser.getTenantId(), loginUser.getTenantCode(), loginUser.getUserId()));
    }

    @GetMapping("/profile/avatar-policy")
    public ApiResponse<UserAvatarPolicyVO> avatarPolicy() {
        requireLoginUser();
        return ApiResponse.ok(userAvatarFileService.policy());
    }

    @OperationLog(module = "User Profile", businessType = "UPDATE")
    @Idempotent("auth:profile:update")
    @PutMapping("/profile")
    public ApiResponse<UserProfileVO> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        LoginUser loginUser = requireLoginUser();
        return ApiResponse.ok(userProfileService.updateProfile(
                loginUser.getTenantId(), loginUser.getTenantCode(), loginUser.getUserId(), request));
    }

    @OperationLog(module = "User Profile", businessType = "UPDATE")
    @Idempotent("auth:profile:avatar")
    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        LoginUser loginUser = requireLoginUser();
        UserAvatarUploadVO uploaded = userAvatarFileService.uploadForUser(
                loginUser.getTenantId(), loginUser.getUserId(), file);
        return ApiResponse.ok(userProfileService.applyAvatar(
                loginUser.getTenantId(), loginUser.getTenantCode(), loginUser.getUserId(), toBindRequest(uploaded)));
    }

    @OperationLog(module = "User Profile", businessType = "UPDATE")
    @Idempotent("auth:password:change")
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        LoginUser loginUser = requireLoginUser();
        userProfileService.changePassword(loginUser.getTenantId(), loginUser.getUserId(), request);
        return ApiResponse.ok();
    }

    @GetMapping("/preference")
    public ApiResponse<UserPreferenceVO> getPreference() {
        LoginUser loginUser = requireLoginUser();
        return ApiResponse.ok(userPreferenceService.getPreference(loginUser.getTenantId(), loginUser.getUserId()));
    }

    @OperationLog(module = "User Preference", businessType = "UPDATE")
    @Idempotent("auth:preference:update")
    @PutMapping("/preference")
    public ApiResponse<UserPreferenceVO> updatePreference(@Valid @RequestBody UserPreferenceUpdateRequest request) {
        LoginUser loginUser = requireLoginUser();
        return ApiResponse.ok(userPreferenceService.updatePreference(
                loginUser.getTenantId(), loginUser.getUserId(), request));
    }

    private LoginUser requireLoginUser() {
        LoginUser loginUser = SecurityContextHelper.getLoginUser();
        if (loginUser == null) {
            throw new UnauthorizedException("error.unauthorized");
        }
        return loginUser;
    }

    private UserAvatarBindRequest toBindRequest(UserAvatarUploadVO uploaded) {
        UserAvatarBindRequest request = new UserAvatarBindRequest();
        request.setAvatarFileId(uploaded.getAvatarFileId());
        request.setAvatarUrl(uploaded.getAvatarUrl());
        request.setAvatarUrlSm(uploaded.getAvatarUrlSm());
        request.setAvatarUrlLg(uploaded.getAvatarUrlLg());
        return request;
    }
}
