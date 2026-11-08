package com.icoolkj.platform.workflow.design;

import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;

/**
 * 设计节点处理器（Strategy）：校验 / 转 BPMN 步骤 / 审批人规则采集。
 * 新增节点类型时新增 Handler Bean，禁止在 ServiceImpl 堆 if。
 */
public interface DesignNodeHandler {

    String nodeType();

    /** 网关类节点不可嵌套在分支内（v1） */
    default boolean nestableInsideBranch() {
        return true;
    }

    /** 是否需要同步 wf_approver_rule */
    default boolean requiresApproverRule() {
        return true;
    }

    void validate(WorkflowModelDesignNodeRequest node, DesignValidationContext context);

    LinearBpmnBuilder.DesignStep toStep(WorkflowModelDesignNodeRequest node, DesignNodeHandlerRegistry registry);
}
