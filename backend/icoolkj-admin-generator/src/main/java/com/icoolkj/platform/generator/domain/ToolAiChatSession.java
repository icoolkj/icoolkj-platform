package com.icoolkj.platform.generator.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ToolAiChatSession {
    private String id;
    private String tenantId;
    private String title;
    private String status;
    private String operatorId;
    private String userId;
    private String operatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
