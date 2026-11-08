package com.icoolkj.platform.workflow.vo;

import lombok.Data;

/** 审批人规则列表/详情 */
@Data
public class WorkflowApproverRuleVO {

    private String id;
    private String tenantId;
    private String modelId;
    private String modelKey;
    private String modelName;
    private String taskDefinitionKey;
    private String variableName;
    private String strategyType;
    private String strategyConfig;
    private String resolveScope;
    private Integer sortNo;
    private String status;
    private String remark;
}
