package com.icoolkj.platform.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新建业务绑定策略 */
@Data
public class WorkflowBizBindingPolicyCreateRequest {

    @NotBlank(message = "{gen.workflowBizBindingPolicy.bizType.required}")
    @Size(max = 64, message = "{gen.workflowBizBindingPolicy.bizType.maxLength}")
    private String bizType;

    @NotBlank(message = "{gen.workflowBizBindingPolicy.bindingMode.required}")
    private String bindingMode;

    private String allowTenantApproverOverride;

    @Size(max = 500, message = "{gen.workflowBizBindingPolicy.remark.maxLength}")
    private String remark;
}
