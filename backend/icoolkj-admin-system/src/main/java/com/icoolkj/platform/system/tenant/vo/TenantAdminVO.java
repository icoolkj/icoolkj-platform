package com.icoolkj.platform.system.tenant.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 平台：租户超级管理员账号视图 */
@Data
public class TenantAdminVO {

    private String userId;
    private String tenantId;
    private String tenantCode;
    private String tenantName;
    private String tenantType;
    private String username;
    private String nickname;
    private String roleCode;
    private String roleName;
    private String status;
    private Integer loginFailCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private String mustChangePassword;
    private String mfaEnabled;
}
