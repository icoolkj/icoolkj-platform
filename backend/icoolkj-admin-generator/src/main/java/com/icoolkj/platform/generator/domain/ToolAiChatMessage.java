package com.icoolkj.platform.generator.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ToolAiChatMessage {
    private String id;
    private String sessionId;
    private String invokeId;
    private String role;
    private String contentPreview;
    private LocalDateTime createdAt;
}
