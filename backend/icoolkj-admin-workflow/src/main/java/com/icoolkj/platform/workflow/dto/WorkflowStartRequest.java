package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.Map;
import lombok.Data;

/** 启动流程请求 */
@Data
public class WorkflowStartRequest {

    @NotBlank
    private String processDefinitionKey;

    @NotBlank
    private String bizType;

    @NotBlank
    private String bizId;

    @NotBlank
    private String bizTitle;

    @NotBlank
    private String tenantId;

    private String modelId;

    private Map<String, Object> variables = Collections.emptyMap();
}
