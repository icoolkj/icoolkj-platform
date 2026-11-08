package com.icoolkj.platform.system.dept.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 部门表 — maps to `sys_dept`. */
@TableName("sys_dept")
@Getter
@Setter
public class DeptEntity extends TenantBaseEntity {

    @TableField("parent_id")
    private String parentId;

    private String ancestors;

    @TableField("dept_name")
    private String deptName;

    @TableField("dept_code")
    private String deptCode;

    @TableField("leader_name")
    private String leaderName;

    private String phone;

    private String email;

    @TableField("sort_no")
    private Integer sortNo;

    private String status;

    private String remark;

}
