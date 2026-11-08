package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 从平台目录同步流程模型到当前租户 */
@Data
public class WorkflowModelSyncRequest {

    @NotBlank(message = "{error.workflow.model_not_found}")
    private String platformModelId;
}
