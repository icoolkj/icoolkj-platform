package com.icoolkj.platform.workflow.design.gateway.handler;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.design.DesignNodeHandlerRegistry;
import com.icoolkj.platform.workflow.design.DesignValidationContext;
import com.icoolkj.platform.workflow.design.gateway.GatewayKind;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** 并行网关 */
@Component
public class ParallelDesignNodeHandler extends AbstractGatewayDesignNodeHandler {

    @Override
    public String nodeType() {
        return GatewayKind.PARALLEL.nodeType();
    }

    @Override
    public void validate(WorkflowModelDesignNodeRequest node, DesignValidationContext context) {
        String key = taskKey(node);
        validateGatewayPlacement(node, context);
        if (CollectionUtils.isEmpty(node.getBranches()) || node.getBranches().size() < 2) {
            throw new BusinessException("error.workflow.design_parallel_branches_required", key);
        }
    }

    @Override
    public LinearBpmnBuilder.DesignStep toStep(WorkflowModelDesignNodeRequest node,
                                               DesignNodeHandlerRegistry registry) {
        return new LinearBpmnBuilder.DesignStep(
                GatewayKind.PARALLEL.nodeType(),
                node.getTaskDefinitionKey().trim(),
                node.getName(),
                "",
                null,
                DesignNodeHandlerRegistry.mapBranches(node.getBranches(), registry),
                null);
    }
}
