package com.icoolkj.platform.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/** 条件分支的一条路径（满足 / 默认） */
@Data
public class WorkflowModelDesignBranchRequest {

    @Size(max = 64)
    private String id;

    @Size(max = 100)
    private String name;

    /** 满足条件时的 Flowable 表达式；默认分支可空 */
    @Size(max = 1000)
    private String expression;

    /** true = 网关 default 出口 */
    @JsonProperty("isDefault")
    private Boolean isDefault;

    /** 该路径上的子节点（approve/cc；当前不允许再嵌套 condition） */
    @Valid
    private List<WorkflowModelDesignNodeRequest> nodes;
}
