package com.icoolkj.platform.system.sitemessage.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 个人收件箱 VO */
@Getter
@Setter
public class SiteMessageInboxVO {

    /** 收件记录 ID */
    private String id;

    private String messageId;
    private String messageSubject;
    private String messageCategory;
    private String messageContent;
    private String senderId;
    private String senderName;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    /** 0 未读 / 1 已读 */
    private String readStatus;
}
