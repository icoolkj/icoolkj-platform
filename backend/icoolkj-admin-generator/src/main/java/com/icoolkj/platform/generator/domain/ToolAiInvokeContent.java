package com.icoolkj.platform.generator.domain;

import lombok.Data;

@Data
public class ToolAiInvokeContent {
    private String invokeId;
    private String systemPrompt;
    private String userMessage;
    private String responseContent;
}
