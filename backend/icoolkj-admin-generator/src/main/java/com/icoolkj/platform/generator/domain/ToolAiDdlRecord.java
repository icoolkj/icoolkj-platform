package com.icoolkj.platform.generator.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ToolAiDdlRecord {
    private String id;
    private String tenantId;
    private String traceId;
    private String invokeId;
    private String recordType;
    private String apiModule;
    private String targetTableName;
    private String generatedSummary;
    private String userPromptPreview;
    private String status;
    private String operatorId;
    private String userId;
    private String operatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
