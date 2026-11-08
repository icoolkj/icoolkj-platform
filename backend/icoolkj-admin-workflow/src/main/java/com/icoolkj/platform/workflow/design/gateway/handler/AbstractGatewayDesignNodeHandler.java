package com.icoolkj.platform.workflow.design.gateway.handler;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.design.DesignNodeHandler;
import com.icoolkj.platform.workflow.design.DesignValidationContext;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;

/** 网关节点 Handler 公共壳：禁止嵌套、不生成 approverRule */
public abstract class AbstractGatewayDesignNodeHandler implements DesignNodeHandler {

    @Override
    public final boolean nestableInsideBranch() {
        return false;
    }

    @Override
    public final boolean requiresApproverRule() {
        return false;
    }

    protected String taskKey(WorkflowModelDesignNodeRequest node) {
        return node.getTaskDefinitionKey() == null ? "" : node.getTaskDefinitionKey().trim();
    }

    protected void validateGatewayPlacement(WorkflowModelDesignNodeRequest node, DesignValidationContext context) {
        String key = taskKey(node);
        context.claimTaskKey(node.getTaskDefinitionKey());
        if (context.isInsideBranch() && !nestableInsideBranch()) {
            throw new BusinessException("error.workflow.design_task_key_invalid", key);
        }
        context.validateBranchChildren(node.getBranches());
    }
}
