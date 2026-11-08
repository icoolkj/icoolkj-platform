package com.icoolkj.platform.system.auth.vo;

import lombok.Data;

import java.util.List;
import com.icoolkj.platform.system.user.vo.LayoutPreferenceVO;

/** 登录用户信息（含 UI 偏好） */
@Data
public class AuthMeVO {

    private String userId;
    private String username;
    private String nickname;
    private String tenantId;
    private String tenantCode;
    private List<String> permissions;
    private String avatarFileId;
    private String avatarUrl;
    private String avatarUrlSm;
    private String avatarUrlLg;
    private String locale;
    private String theme;
    private LayoutPreferenceVO layout;
    /** 初始/重置密码后需先修改密码才能进入系统 */
    private Boolean mustChangePassword;
    /** 当前是否已绑定 MFA */
    private Boolean mfaEnabled;
    /** 会话要求强制绑定 MFA */
    private Boolean mustEnrollMfa;
}
