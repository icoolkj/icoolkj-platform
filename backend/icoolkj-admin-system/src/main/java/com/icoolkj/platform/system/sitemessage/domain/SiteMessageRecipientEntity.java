package com.icoolkj.platform.system.sitemessage.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 站内信收件表 — maps to `sys_site_message_recipient`. */
@TableName("sys_site_message_recipient")
@Getter
@Setter
public class SiteMessageRecipientEntity {

    @TableId
    private String id;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("message_id")
    private String messageId;

    @TableField("user_id")
    private String userId;

    @TableField("read_at")
    private LocalDateTime readAt;

    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
