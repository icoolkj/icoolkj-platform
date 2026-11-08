package com.icoolkj.platform.workflow.design.handler;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.design.DesignNodeHandler;
import com.icoolkj.platform.workflow.design.DesignNodeHandlerRegistry;
import com.icoolkj.platform.workflow.design.DesignValidationContext;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import com.icoolkj.platform.workflow.support.WorkflowNodeFieldPermSupport;
import com.icoolkj.platform.workflow.support.WorkflowPassModes;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 审批节点 */
@Component
public class ApproveDesignNodeHandler implements DesignNodeHandler {

    @Override
    public String nodeType() {
        return LinearBpmnBuilder.NODE_APPROVE;
    }

    @Override
    public void validate(WorkflowModelDesignNodeRequest node, DesignValidationContext context) {
        context.claimTaskKey(node.getTaskDefinitionKey());
        context.claimVariableName(node.getVariableName());
        context.validateApproverStrategy(node.getStrategyType(), node.getStrategyConfig());
        String passMode = WorkflowPassModes.normalize(node.getPassMode());
        if (!LinearBpmnBuilder.PASS_SEQUENTIAL.equals(passMode)
                && !LinearBpmnBuilder.PASS_ANY.equals(passMode)
                && !LinearBpmnBuilder.PASS_ALL.equals(passMode)
                && !LinearBpmnBuilder.PASS_SEQUENTIAL_ALL.equals(passMode)) {
            throw new BusinessException("error.workflow.design_task_key_invalid",
                    node.getTaskDefinitionKey());
        }
        try {
            WorkflowNodeFieldPermSupport.assertValid(node.getFieldPerms());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("error.workflow.design_field_perm_invalid",
                    node.getTaskDefinitionKey());
        }
        node.setFieldPerms(WorkflowNodeFieldPermSupport.normalizeMap(node.getFieldPerms()));
    }

    @Override
    public LinearBpmnBuilder.DesignStep toStep(WorkflowModelDesignNodeRequest node,
                                               DesignNodeHandlerRegistry registry) {
        return new LinearBpmnBuilder.DesignStep(
                LinearBpmnBuilder.NODE_APPROVE,
                node.getTaskDefinitionKey().trim(),
                node.getName(),
                node.getVariableName().trim(),
                null,
                null,
                WorkflowPassModes.normalize(node.getPassMode()),
                StringUtils.hasText(node.getDueDuration()) ? node.getDueDuration().trim() : null,
                StringUtils.hasText(node.getTimeoutAction()) ? node.getTimeoutAction().trim() : null);
    }
}
