package com.icoolkj.platform.system.user.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/** Maps to `sys_user_post`. */
@TableName("sys_user_post")
@Getter
@Setter
public class UserPostEntity {

    @TableField("tenant_id")
    private String tenantId;

    @TableField("user_id")
    private String userId;

    @TableField("post_id")
    private String postId;

}
