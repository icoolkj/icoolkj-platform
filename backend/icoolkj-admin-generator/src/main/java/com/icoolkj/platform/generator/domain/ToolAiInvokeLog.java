package com.icoolkj.platform.generator.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ToolAiInvokeLog {
    private String id;
    private String tenantId;
    private String traceId;
    private String providerName;
    private String model;
    private String bizType;
    private String bizRefId;
    private String requestUrl;
    private String responseStatus;
    private String errorMessage;
    private Integer costTime;
    private String userPromptPreview;
    private String responsePreview;
    private String operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
}
