package com.icoolkj.platform.system.user.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 当前登录用户个人资料 */
@Data
public class UserProfileVO {

    private String userId;
    private String username;
    private String nickname;
    private String realName;
    private String email;
    private String phone;
    private String gender;
    private String avatarUrl;
    private String avatarFileId;
    private String avatarUrlSm;
    private String avatarUrlMd;
    private String avatarUrlLg;

    private String tenantId;
    private String tenantCode;
    private String tenantName;

    private String deptId;
    private String deptName;

    private List<UserProfileRoleVO> roles;
    private List<UserProfilePostVO> posts;

    private String lastLoginIp;
    private LocalDateTime lastLoginAt;
    private LocalDateTime pwdUpdatedAt;
    private String mustChangePassword;
}
