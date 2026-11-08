package com.icoolkj.platform.system.user.domain;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 用户表 — maps to `sys_user`. */
@TableName("sys_user")
@Getter
@Setter
public class UserEntity extends TenantBaseEntity {

    @TableField("dept_id")
    private String deptId;

    @TableField(exist = false)
    private String deptName;

    private String username;

    private String nickname;

    @TableField("real_name")
    private String realName;

    @TableField("user_type")
    private String userType;

    private String email;

    private String phone;

    private String gender;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("avatar_url_sm")
    private String avatarUrlSm;

    @TableField("avatar_url_lg")
    private String avatarUrlLg;

    @TableField("avatar_file_id")
    private String avatarFileId;

    @TableField("password_hash")
    private String passwordHash;

    private String status;

    @TableField("is_super_admin")
    private String isSuperAdmin;

    @TableField("login_fail_count")
    private Integer loginFailCount;

    @TableField("locked_until")
    private LocalDateTime lockedUntil;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @TableField("pwd_updated_at")
    private LocalDateTime pwdUpdatedAt;

    @TableField("must_change_password")
    private String mustChangePassword;

    @TableField("mfa_enabled")
    private String mfaEnabled;

    @TableField("mfa_secret_enc")
    private String mfaSecretEnc;

    @TableField("mfa_bound_at")
    private LocalDateTime mfaBoundAt;

    private String remark;

}
