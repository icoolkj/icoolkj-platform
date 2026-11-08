package com.icoolkj.platform.workflow.vo;

import lombok.Data;

/** 业务绑定策略 */
@Data
public class WorkflowBizBindingPolicyVO {

    private String id;
    private String bizType;
    private String bindingMode;
    private String allowTenantApproverOverride;
    private String remark;
}
