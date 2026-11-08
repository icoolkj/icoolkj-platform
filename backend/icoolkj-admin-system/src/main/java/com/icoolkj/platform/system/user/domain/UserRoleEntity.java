package com.icoolkj.platform.system.user.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/** Maps to `sys_user_role`. */
@TableName("sys_user_role")
@Getter
@Setter
public class UserRoleEntity {

    @TableField("tenant_id")
    private String tenantId;

    @TableField("user_id")
    private String userId;

    @TableField("role_id")
    private String roleId;

}
