package com.icoolkj.platform.infra.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 第三方调用日志列表/详情 */
@Data
public class ThirdPartyCallLogVO {

    private String id;
    private String tenantId;
    private String providerName;
    private String bizType;
    private String requestUrl;
    private String requestMethod;
    private String requestData;
    private String responseData;
    private String responseStatus;
    private String errorMessage;
    private Integer costTime;
    private LocalDateTime calledAt;

}

