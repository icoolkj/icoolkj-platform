package com.icoolkj.platform.workflow.design.gateway.handler;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.design.DesignNodeHandlerRegistry;
import com.icoolkj.platform.workflow.design.DesignValidationContext;
import com.icoolkj.platform.workflow.design.gateway.GatewayKind;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignBranchRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 排他 / 包容网关：共享 branches + expression 校验与 DesignStep 映射。
 */
public abstract class AbstractConditionLikeDesignNodeHandler extends AbstractGatewayDesignNodeHandler {

    private final GatewayKind kind;

    protected AbstractConditionLikeDesignNodeHandler(GatewayKind kind) {
        if (!kind.isConditionLike()) {
            throw new IllegalArgumentException("Expected condition-like gateway: " + kind);
        }
        this.kind = kind;
    }

    @Override
    public final String nodeType() {
        return kind.nodeType();
    }

    @Override
    public void validate(WorkflowModelDesignNodeRequest node, DesignValidationContext context) {
        String key = taskKey(node);
        validateGatewayPlacement(node, context);
        validateConditionLikeBranches(node, key);
    }

    @Override
    public LinearBpmnBuilder.DesignStep toStep(WorkflowModelDesignNodeRequest node,
                                               DesignNodeHandlerRegistry registry) {
        return new LinearBpmnBuilder.DesignStep(
                kind.nodeType(),
                node.getTaskDefinitionKey().trim(),
                node.getName(),
                "",
                node.getConditionExpression(),
                DesignNodeHandlerRegistry.mapBranches(node.getBranches(), registry),
                null);
    }

    static void validateConditionLikeBranches(WorkflowModelDesignNodeRequest node, String key) {
        if (CollectionUtils.isEmpty(node.getBranches())) {
            if (!StringUtils.hasText(node.getConditionExpression())) {
                throw new BusinessException("error.workflow.design_condition_expression_required", key);
            }
            return;
        }
        boolean hasYes = false;
        for (WorkflowModelDesignBranchRequest br : node.getBranches()) {
            if (Boolean.TRUE.equals(br.getIsDefault())) {
                continue;
            }
            hasYes = true;
            if (!StringUtils.hasText(br.getExpression())) {
                throw new BusinessException("error.workflow.design_condition_expression_required", key);
            }
        }
        if (!hasYes) {
            throw new BusinessException("error.workflow.design_condition_expression_required", key);
        }
    }
}
