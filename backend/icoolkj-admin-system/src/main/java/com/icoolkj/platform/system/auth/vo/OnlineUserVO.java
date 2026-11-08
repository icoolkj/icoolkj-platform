package com.icoolkj.platform.system.auth.vo;

import java.time.Instant;
import lombok.Data;

/** Online login session row for monitor. */
@Data
public class OnlineUserVO {

    private String sessionId;
    private String userId;
    private String username;
    private String tenantId;
    private String tenantCode;
    private Instant createdAt;
}
