package com.icoolkj.platform.workflow.design.handler;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import com.icoolkj.platform.workflow.design.DesignNodeHandler;
import com.icoolkj.platform.workflow.design.DesignNodeHandlerRegistry;
import com.icoolkj.platform.workflow.design.DesignValidationContext;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import com.icoolkj.platform.workflow.support.WorkflowStrategyConfigSupport;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 子流程 callActivity 节点 */
@Component
public class SubprocessDesignNodeHandler implements DesignNodeHandler {

    private final WorkflowStrategyConfigSupport strategyConfigSupport;

    public SubprocessDesignNodeHandler(WorkflowStrategyConfigSupport strategyConfigSupport) {
        this.strategyConfigSupport = strategyConfigSupport;
    }

    @Override
    public String nodeType() {
        return DesignNodeTypes.SUBPROCESS;
    }

    @Override
    public boolean nestableInsideBranch() {
        return false;
    }

    @Override
    public boolean requiresApproverRule() {
        return false;
    }

    @Override
    public void validate(WorkflowModelDesignNodeRequest node, DesignValidationContext context) {
        context.claimTaskKey(node.getTaskDefinitionKey());
        String key = node.getTaskDefinitionKey() == null ? "" : node.getTaskDefinitionKey().trim();
        if (context.isInsideBranch() && !nestableInsideBranch()) {
            throw new BusinessException("error.workflow.design_node_not_nestable", key, nodeType());
        }
        try {
            strategyConfigSupport.requireCalledElement(node.getStrategyConfig(), key);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("error.workflow.design_subprocess_called_required", key);
        }
    }

    @Override
    public LinearBpmnBuilder.DesignStep toStep(WorkflowModelDesignNodeRequest node,
                                               DesignNodeHandlerRegistry registry) {
        return new LinearBpmnBuilder.DesignStep(
                DesignNodeTypes.SUBPROCESS,
                node.getTaskDefinitionKey().trim(),
                node.getName(),
                null,
                null,
                null,
                null,
                null,
                null,
                StringUtils.hasText(node.getStrategyConfig()) ? node.getStrategyConfig().trim() : null);
    }
}
