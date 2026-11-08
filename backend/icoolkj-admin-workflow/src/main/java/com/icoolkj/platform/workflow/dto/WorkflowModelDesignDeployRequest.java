package com.icoolkj.platform.workflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/** 竖向设计器：生成 BPMN 部署并同步审批人规则 */
@Data
public class WorkflowModelDesignDeployRequest {

    @NotBlank(message = "{validation.id.required}")
    private String modelId;

    @NotEmpty
    @Valid
    private List<WorkflowModelDesignNodeRequest> nodes;

    /** 流程全局配置 JSON（可选，优先经本接口保存） */
    @Size(max = 65535)
    private String processConfig;

    @Size(max = 500, message = "{gen.workflowModel.remark.maxLength}")
    private String remark;
}
