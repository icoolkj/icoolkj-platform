package com.icoolkj.platform.common.model.log;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiAccessLogRecord {

    private String tenantId;
    private String traceId;
    private String requestUri;
    private String requestMethod;
    private String clientIp;
    private String userId;
    private String username;
    private String requestParam;
    private Integer responseCode;
    private String responseMessage;
    private Integer costTime;
    private LocalDateTime accessedAt;
}
