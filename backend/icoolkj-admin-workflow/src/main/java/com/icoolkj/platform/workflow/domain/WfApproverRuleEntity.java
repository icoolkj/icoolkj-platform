package com.icoolkj.platform.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 流程节点审批人规则 — maps to {@code wf_approver_rule}。
 * <p>平台覆盖表：{@code BaseEntity} + 自有 {@code tenantId}（0=平台默认）。
 */
@TableName("wf_approver_rule")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WfApproverRuleEntity extends BaseEntity {

    @TableField("tenant_id")
    private String tenantId;

    @TableField("model_id")
    private String modelId;

    @TableField("task_definition_key")
    private String taskDefinitionKey;

    @TableField("variable_name")
    private String variableName;

    @TableField("strategy_type")
    private String strategyType;

    @TableField("strategy_config")
    private String strategyConfig;

    @TableField("resolve_scope")
    private String resolveScope;

    @TableField("sort_no")
    private Integer sortNo;

    private String status;

    private String remark;
}
