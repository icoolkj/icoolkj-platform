package com.icoolkj.platform.system.user.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 用户表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserPageQuery extends PageQuery {
    private String deptId;
    private String deptSubtreeRootId;
    private String username;
    private String nickname;
    private String realName;
    private String userType;
    private String email;
    private String phone;
    private String gender;
    private String avatarUrl;
    private String status;
    private String isSuperAdmin;
    private Integer loginFailCount;
    private LocalDateTime lockedUntil;
    private String lastLoginIp;
    private LocalDateTime lastLoginAt;
    private LocalDateTime pwdUpdatedAt;
    private String mustChangePassword;
    private String remark;
}
