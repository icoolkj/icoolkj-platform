package com.icoolkj.platform.system.loginlog.domain;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 登录日志表 — maps to `sys_login_log`. */
@TableName("sys_login_log")
@Getter
@Setter
public class LoginLogEntity extends TenantBaseEntity {

    private String username;

    @TableField("login_type")
    private String loginType;

    @TableField("login_status")
    private String loginStatus;

    private String message;

    private String ip;

    private String location;

    @TableField("user_agent")
    private String userAgent;

    private String browser;

    private String os;

    @TableField("login_at")
    private LocalDateTime loginAt;

    @TableField("prev_hash")
    private String prevHash;

    @TableField("row_hash")
    private String rowHash;

}
