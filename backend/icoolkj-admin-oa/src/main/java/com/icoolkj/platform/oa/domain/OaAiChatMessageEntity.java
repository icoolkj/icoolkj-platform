package com.icoolkj.platform.oa.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 办公助手 AI 消息 — maps to {@code oa_ai_chat_message}. */
@TableName("oa_ai_chat_message")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaAiChatMessageEntity extends TenantBaseEntity {

    @TableField("session_id")
    private String sessionId;

    /** user / assistant */
    private String role;

    private String content;

    @TableField("used_skills_json")
    private String usedSkillsJson;
}
