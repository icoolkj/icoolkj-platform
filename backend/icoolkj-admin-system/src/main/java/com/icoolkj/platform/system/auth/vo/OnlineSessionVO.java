package com.icoolkj.platform.system.auth.vo;

import java.time.Instant;
import lombok.Data;

/** 在线会话 */
@Data
public class OnlineSessionVO {

    private String sessionId;
    private String userId;
    private String username;
    private String tenantId;
    private String tenantCode;
    private Instant createdAt;
}
