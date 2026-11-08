package com.icoolkj.platform.system.sitemessage.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 管理端站内信查收情况 */
@Getter
@Setter
public class SiteMessageRecipientStatusVO {

    private String id;

    private String userId;

    private String userNickname;

    private String username;

    private String readStatus;

    private LocalDateTime readAt;
}
