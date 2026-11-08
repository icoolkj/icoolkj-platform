package com.icoolkj.platform.system.role.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/** Maps to `sys_role_menu`. */
@TableName("sys_role_menu")
@Getter
@Setter
public class RoleMenuEntity {

    @TableField("tenant_id")
    private String tenantId;

    @TableField("role_id")
    private String roleId;

    @TableField("menu_id")
    private String menuId;

}
