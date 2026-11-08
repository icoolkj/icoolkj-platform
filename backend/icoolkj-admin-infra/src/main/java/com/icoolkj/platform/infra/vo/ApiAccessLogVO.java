package com.icoolkj.platform.infra.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 接口访问日志表列表/详情 */
@Data
public class ApiAccessLogVO {
    private String id;
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
