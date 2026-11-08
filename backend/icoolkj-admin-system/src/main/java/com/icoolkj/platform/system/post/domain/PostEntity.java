package com.icoolkj.platform.system.post.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 岗位表 — maps to `sys_post`. */
@TableName("sys_post")
@Getter
@Setter
public class PostEntity extends TenantBaseEntity {

    @TableField("post_name")
    private String postName;

    @TableField("post_code")
    private String postCode;

    @TableField("sort_no")
    private Integer sortNo;

    private String status;

    private String remark;

}
