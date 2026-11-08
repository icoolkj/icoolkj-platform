package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ToolAiDdlRecordVO {
    private String id;
    private String traceId;
    private String invokeId;
    private String recordType;
    private String apiModule;
    private String targetTableName;
    private String generatedSummary;
    private String userPromptPreview;
    private String status;
    private String operatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userPrompt;
    private String generatedDdl;
    private String finalDdl;
}
