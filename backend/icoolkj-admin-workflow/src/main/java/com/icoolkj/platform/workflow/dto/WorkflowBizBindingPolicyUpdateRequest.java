package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 更新业务绑定策略 */
@Data
public class WorkflowBizBindingPolicyUpdateRequest {

    @NotBlank(message = "{gen.workflowBizBindingPolicy.id.required}")
    private String id;

    @NotBlank(message = "{gen.workflowBizBindingPolicy.bindingMode.required}")
    private String bindingMode;

    private String allowTenantApproverOverride;

    @Size(max = 500, message = "{gen.workflowBizBindingPolicy.remark.maxLength}")
    private String remark;
}
