package com.icoolkj.platform.system.user.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 用户表列表/详情 */
@Data
public class UserVO {
    private String id;
    private String deptId;
    private String deptName;
    private String username;
    private String nickname;
    private String realName;
    private String userType;
    private String email;
    private String phone;
    private String gender;
    private String avatarUrl;
    private String avatarFileId;
    private String avatarUrlSm;
    private String status;
    private String isSuperAdmin;
    private Integer loginFailCount;
    private LocalDateTime lockedUntil;
    private String lastLoginIp;
    private LocalDateTime lastLoginAt;
    private LocalDateTime pwdUpdatedAt;
    private String mustChangePassword;
    private String remark;
    private LocalDateTime createdAt;
}
