package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 流程模型部署请求 */
@Data
public class WorkflowModelDeployRequest {

    @NotBlank(message = "{validation.id.required}")
    private String modelId;

    @NotBlank(message = "{gen.workflowModel.bpmnXml.required}")
    private String bpmnXml;

    @Size(max = 500, message = "{gen.workflowModel.remark.maxLength}")
    private String remark;
}
