package com.icoolkj.platform.workflow.design.handler;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import com.icoolkj.platform.workflow.design.DesignNodeHandler;
import com.icoolkj.platform.workflow.design.DesignNodeHandlerRegistry;
import com.icoolkj.platform.workflow.design.DesignValidationContext;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import java.time.Duration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 延时等待节点 */
@Component
public class DelayDesignNodeHandler implements DesignNodeHandler {

    @Override
    public String nodeType() {
        return DesignNodeTypes.DELAY;
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
        if (!StringUtils.hasText(node.getDueDuration())) {
            throw new BusinessException("error.workflow.design_delay_duration_required", key);
        }
        try {
            Duration.parse(node.getDueDuration().trim());
        } catch (Exception ex) {
            throw new BusinessException("error.workflow.design_delay_duration_invalid", key);
        }
    }

    @Override
    public LinearBpmnBuilder.DesignStep toStep(WorkflowModelDesignNodeRequest node,
                                               DesignNodeHandlerRegistry registry) {
        return new LinearBpmnBuilder.DesignStep(
                DesignNodeTypes.DELAY,
                node.getTaskDefinitionKey().trim(),
                node.getName(),
                null,
                null,
                null,
                null,
                node.getDueDuration().trim(),
                null,
                null);
    }
}
