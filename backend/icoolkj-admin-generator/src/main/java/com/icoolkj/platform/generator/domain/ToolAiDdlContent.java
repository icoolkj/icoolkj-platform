package com.icoolkj.platform.generator.domain;

import lombok.Data;

@Data
public class ToolAiDdlContent {
    private String ddlRecordId;
    private String userPrompt;
    private String generatedDdl;
    private String finalDdl;
}
