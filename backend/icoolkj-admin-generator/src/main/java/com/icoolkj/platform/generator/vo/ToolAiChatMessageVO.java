package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ToolAiChatMessageVO {
    private String id;
    private String sessionId;
    private String invokeId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
