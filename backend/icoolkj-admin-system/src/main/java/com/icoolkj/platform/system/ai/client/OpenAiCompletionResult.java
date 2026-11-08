package com.icoolkj.platform.system.ai.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class OpenAiCompletionResult {

    private String content;
    private String finishReason;
    private List<ToolCall> toolCalls = new ArrayList<>();
    private Object rawAssistantMessage;

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCall {
        private String id;
        private String name;
        private String argumentsJson;
    }
}
