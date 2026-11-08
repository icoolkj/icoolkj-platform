package com.icoolkj.platform.system.user.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 用户密码历史 — maps to {@code sys_user_password_history}. */
@Data
@TableName("sys_user_password_history")
public class UserPasswordHistoryEntity {

    @TableId("id")
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("user_id")
    private String userId;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
