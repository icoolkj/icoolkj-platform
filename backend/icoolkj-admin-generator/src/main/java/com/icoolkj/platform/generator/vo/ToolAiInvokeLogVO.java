package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ToolAiInvokeLogVO {
    private String id;
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
    private String operatorName;
    private LocalDateTime createdAt;
    private String systemPrompt;
    private String userMessage;
    private String responseContent;
}
