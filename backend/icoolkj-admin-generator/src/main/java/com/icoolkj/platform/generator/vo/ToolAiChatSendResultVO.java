package com.icoolkj.platform.generator.vo;

import lombok.Data;

@Data
public class ToolAiChatSendResultVO {
    private ToolAiChatMessageVO userMessage;
    private ToolAiChatMessageVO assistantMessage;
}
