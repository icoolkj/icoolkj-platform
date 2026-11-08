package com.icoolkj.platform.oa.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/** 办公助手 AI 会话 — maps to {@code oa_ai_chat_session}. */
@TableName("oa_ai_chat_session")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaAiChatSessionEntity extends TenantBaseEntity {

    @TableField("user_id")
    private String userId;

    private String title;

    /** 0 正常 / 1 归档 */
    private String status;

    @TableField("last_message_at")
    private LocalDateTime lastMessageAt;

    @TableField("message_count")
    private Integer messageCount;
}
