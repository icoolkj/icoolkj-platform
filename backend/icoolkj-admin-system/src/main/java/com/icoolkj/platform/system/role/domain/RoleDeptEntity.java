package com.icoolkj.platform.system.role.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/** Maps to `sys_role_dept`. */
@TableName("sys_role_dept")
@Getter
@Setter
public class RoleDeptEntity {

    @TableField("tenant_id")
    private String tenantId;

    @TableField("role_id")
    private String roleId;

    @TableField("dept_id")
    private String deptId;

}
