package com.icoolkj.platform.system.sitemessage.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 站内信主表 — maps to `sys_site_message`. */
@TableName("sys_site_message")
@Getter
@Setter
public class SiteMessageEntity extends TenantBaseEntity {

    @TableField("message_subject")
    private String messageSubject;

    @TableField("message_category")
    private String messageCategory;

    @TableField("message_content")
    private String messageContent;

    @TableField("sender_id")
    private String senderId;

    @TableField("user_id")
    private String userId;

    @TableField("sender_name")
    private String senderName;

    @TableField("sent_at")
    private LocalDateTime sentAt;
}
