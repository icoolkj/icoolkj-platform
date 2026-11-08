package com.icoolkj.platform.system.sitemessage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** 发送站内信 */
@Getter
@Setter
public class SiteMessageSendRequest {

    @NotBlank(message = "{gen.siteMessage.messageSubject.required}")
    @Size(max = 200, message = "{gen.siteMessage.messageSubject.maxLength}")
    private String messageSubject;

    @NotBlank(message = "{gen.siteMessage.messageCategory.required}")
    @Size(max = 20, message = "{gen.siteMessage.messageCategory.maxLength}")
    private String messageCategory;

    @NotBlank(message = "{gen.siteMessage.messageContent.required}")
    private String messageContent;

    /** 平台超管跨租户发送时指定单个目标租户 */
    private String targetTenantId;

    /** 平台超管一次向多个租户发送（仅支持 recipientType=all） */
    private List<String> targetTenantIds;

    /** user / dept / role / all */
    @NotBlank(message = "{gen.siteMessage.recipientType.required}")
    private String recipientType;

    private List<String> userIds;

    private List<String> deptIds;

    private List<String> roleIds;
}
