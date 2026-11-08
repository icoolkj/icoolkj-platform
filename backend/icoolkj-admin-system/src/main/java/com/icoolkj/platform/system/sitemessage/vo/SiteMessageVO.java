package com.icoolkj.platform.system.sitemessage.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 管理端站内信 VO */
@Getter
@Setter
public class SiteMessageVO {

    private String id;
    private String tenantId;
    private String messageSubject;
    private String messageCategory;
    private String messageContent;
    private String senderId;
    private String senderName;
    private LocalDateTime sentAt;
    private Long recipientCount;
}
