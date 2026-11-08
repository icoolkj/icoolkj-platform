package com.icoolkj.platform.workflow.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 审批人规则分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WorkflowApproverRulePageQuery extends PageQuery {

    private String modelId;
    private String modelKey;
    private String taskDefinitionKey;
    private String strategyType;
    private String status;
    /** 规则归属租户（平台=0，租户=当前租户） */
    private String scopeTenantId;
    /** 租户列表是否联查平台（tenant_id=0）默认规则 */
    private boolean includePlatform;
}
