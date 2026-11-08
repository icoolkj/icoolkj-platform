package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ToolAiChatSessionVO {
    private String id;
    private String title;
    private String status;
    private String operatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ToolAiChatMessageVO> messages = new ArrayList<>();
}
