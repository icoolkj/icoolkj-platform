package com.icoolkj.platform.workflow.design;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignBranchRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 设计节点 Handler 注册表：按 nodeType 解析，禁止散落 if */
@Component
public class DesignNodeHandlerRegistry {

    private final Map<String, DesignNodeHandler> handlersByType;

    public DesignNodeHandlerRegistry(List<DesignNodeHandler> handlers) {
        Map<String, DesignNodeHandler> map = new LinkedHashMap<>();
        if (handlers != null) {
            for (DesignNodeHandler handler : handlers) {
                map.put(normalize(handler.nodeType()), handler);
            }
        }
        this.handlersByType = Map.copyOf(map);
    }

    public DesignNodeHandler require(String nodeType) {
        String type = normalize(nodeType);
        DesignNodeHandler handler = handlersByType.get(type);
        if (handler == null) {
            throw new BusinessException("error.workflow.design_task_key_invalid", type);
        }
        return handler;
    }

    public void validateTree(List<WorkflowModelDesignNodeRequest> nodes, DesignValidationContext rootCtx) {
        rootCtx.validateChildNodes(nodes);
    }

    public List<LinearBpmnBuilder.DesignStep> toSteps(List<WorkflowModelDesignNodeRequest> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return List.of();
        }
        List<LinearBpmnBuilder.DesignStep> steps = new ArrayList<>(nodes.size());
        for (WorkflowModelDesignNodeRequest node : nodes) {
            steps.add(require(node.getNodeType()).toStep(node, this));
        }
        return steps;
    }

    public void walkApproverRuleNodes(List<WorkflowModelDesignNodeRequest> nodes,
                                      Consumer<WorkflowModelDesignNodeRequest> consumer) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (WorkflowModelDesignNodeRequest node : nodes) {
            DesignNodeHandler handler = require(node.getNodeType());
            if (handler.requiresApproverRule()) {
                consumer.accept(node);
            }
            if (!CollectionUtils.isEmpty(node.getBranches())) {
                for (WorkflowModelDesignBranchRequest br : node.getBranches()) {
                    walkApproverRuleNodes(br.getNodes(), consumer);
                }
            }
        }
    }

    public static String normalize(String nodeType) {
        if (!StringUtils.hasText(nodeType)) {
            return LinearBpmnBuilder.NODE_APPROVE;
        }
        return nodeType.trim().toLowerCase(Locale.ROOT);
    }

    public static List<LinearBpmnBuilder.DesignBranch> mapBranches(
            List<WorkflowModelDesignBranchRequest> branches,
            DesignNodeHandlerRegistry registry) {
        if (CollectionUtils.isEmpty(branches)) {
            return List.of();
        }
        List<LinearBpmnBuilder.DesignBranch> out = new ArrayList<>(branches.size());
        for (WorkflowModelDesignBranchRequest br : branches) {
            boolean isDefault = Boolean.TRUE.equals(br.getIsDefault());
            String branchId = StringUtils.hasText(br.getId())
                    ? br.getId().trim()
                    : (isDefault ? "default" : "yes");
            out.add(new LinearBpmnBuilder.DesignBranch(
                    branchId,
                    br.getName(),
                    br.getExpression(),
                    isDefault,
                    registry.toSteps(br.getNodes())));
        }
        return out;
    }
}
