package com.icoolkj.platform.workflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** 竖向设计器节点：approve / cc / condition */
@Data
public class WorkflowModelDesignNodeRequest {

    /** approve | cc | condition，默认 approve */
    @Size(max = 32)
    private String nodeType;

    @NotBlank(message = "{gen.workflowApproverRule.taskDefinitionKey.required}")
    @Size(max = 64)
    private String taskDefinitionKey;

    @Size(max = 100)
    private String name;

    /** 审批/抄送指派人变量；condition 节点可不填 */
    @Size(max = 64)
    private String variableName;

    /**
     * 兼容旧版：条件表达式（无 branches 时当作「满足」出口）。
     * 有 branches 时优先用非默认分支的 expression。
     */
    @Size(max = 1000)
    private String conditionExpression;

    /** condition / parallel 的分叉路径；parallel 无 expression/default */
    @Valid
    private List<WorkflowModelDesignBranchRequest> branches;

    /**
     * approve 通过方式：sequential | any | all。
     * any=或签，all=会签（multiInstance）。
     */
    @Size(max = 32)
    private String passMode;

    /** approve/cc 策略；网关节点不要求 */
    @Size(max = 32)
    private String strategyType;

    @Size(max = 1000)
    private String strategyConfig;

    @Size(max = 32)
    private String resolveScope;

    private Integer sortNo;

    /** end | previous | starter | starterEdit；缺省 end */
    @Size(max = 32)
    private String rejectMode;

    /** ISO-8601 时限，如 PT24H；空=不挂 boundary timer */
    @Size(max = 64)
    private String dueDuration;

    /** notify | rejectEnd；缺省 notify */
    @Size(max = 32)
    private String timeoutAction;

    /** 缺省 true */
    private Boolean allowTransfer;

    /** 缺省 true */
    private Boolean allowDelegate;

    /** 缺省 true */
    private Boolean allowAddSign;

    /**
     * 节点 × 字段权限：fieldCode → R|W|H。
     * 仅 approve/cc 有意义；缺省字段按 R。存 design_json，随版本冻结。
     */
    private Map<String, String> fieldPerms;
}
