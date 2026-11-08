package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 更新审批人规则 */
@Data
public class WorkflowApproverRuleUpdateRequest {

    @NotBlank(message = "{gen.workflowApproverRule.id.required}")
    private String id;

    @NotBlank(message = "{gen.workflowApproverRule.variableName.required}")
    @Size(max = 64, message = "{gen.workflowApproverRule.variableName.maxLength}")
    private String variableName;

    @NotBlank(message = "{gen.workflowApproverRule.strategyType.required}")
    private String strategyType;

    @NotBlank(message = "{gen.workflowApproverRule.strategyConfig.required}")
    @Size(max = 1000, message = "{gen.workflowApproverRule.strategyConfig.maxLength}")
    private String strategyConfig;

    @NotBlank(message = "{gen.workflowApproverRule.resolveScope.required}")
    private String resolveScope;

    @NotNull(message = "{gen.workflowApproverRule.sortNo.required}")
    private Integer sortNo;

    private String status;

    @Size(max = 500, message = "{gen.workflowApproverRule.remark.maxLength}")
    private String remark;
}
