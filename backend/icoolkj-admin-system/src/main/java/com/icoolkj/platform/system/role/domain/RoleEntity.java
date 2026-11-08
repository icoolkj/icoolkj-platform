package com.icoolkj.platform.system.role.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 角色表 — maps to `sys_role`. */
@TableName("sys_role")
@Getter
@Setter
public class RoleEntity extends TenantBaseEntity {

    @TableField("role_name")
    private String roleName;

    @TableField("role_code")
    private String roleCode;

    @TableField("data_scope")
    private String dataScope;

    @TableField("sort_no")
    private Integer sortNo;

    private String status;

    @TableField("is_super_role")
    private String isSuperRole;

    private String remark;

}
