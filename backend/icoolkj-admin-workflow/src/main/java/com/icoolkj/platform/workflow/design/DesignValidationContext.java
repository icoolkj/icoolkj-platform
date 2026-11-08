package com.icoolkj.platform.workflow.design;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignBranchRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.support.WorkflowApproverRuleSupport;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 设计树校验上下文：去重键、变量名、嵌套深度、审批规则校验器 */
public final class DesignValidationContext {

    public static final Pattern TASK_KEY = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{0,63}$");
    public static final Pattern VAR_NAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{0,63}$");

    private final Set<String> taskKeys = new LinkedHashSet<>();
    private final Set<String> variableNames = new HashSet<>();
    private final boolean insideBranch;
    private final WorkflowApproverRuleSupport approverRuleSupport;
    private final DesignNodeHandlerRegistry registry;

    public DesignValidationContext(WorkflowApproverRuleSupport approverRuleSupport,
                                   DesignNodeHandlerRegistry registry,
                                   boolean insideBranch) {
        this.approverRuleSupport = approverRuleSupport;
        this.registry = registry;
        this.insideBranch = insideBranch;
    }

    public boolean isInsideBranch() {
        return insideBranch;
    }

    public DesignValidationContext childBranch() {
        return new DesignValidationContext(approverRuleSupport, registry, true);
    }

    public void claimTaskKey(String rawKey) {
        String key = rawKey == null ? "" : rawKey.trim();
        if (!TASK_KEY.matcher(key).matches()) {
            throw new BusinessException("error.workflow.design_task_key_invalid", key);
        }
        if (!taskKeys.add(key)) {
            throw new BusinessException("error.workflow.design_task_key_duplicate", key);
        }
    }

    public void claimVariableName(String rawVar) {
        String var = rawVar == null ? "" : rawVar.trim();
        if (!VAR_NAME.matcher(var).matches()) {
            throw new BusinessException("error.workflow.design_variable_invalid", var);
        }
        if (!variableNames.add(var)) {
            throw new BusinessException("error.workflow.design_variable_duplicate", var);
        }
    }

    public void validateApproverStrategy(String strategyType, String strategyConfig) {
        if (!StringUtils.hasText(strategyType) || !StringUtils.hasText(strategyConfig)) {
            throw new BusinessException("error.workflow.approver_rule_invalid");
        }
        approverRuleSupport.validateStrategyConfig(strategyType, strategyConfig);
    }

    public void validateChildNodes(List<WorkflowModelDesignNodeRequest> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (WorkflowModelDesignNodeRequest node : nodes) {
            registry.require(node.getNodeType()).validate(node, this);
        }
    }

    public void validateBranchChildren(List<WorkflowModelDesignBranchRequest> branches) {
        if (CollectionUtils.isEmpty(branches)) {
            return;
        }
        DesignValidationContext childCtx = childBranch();
        // share uniqueness across tree
        childCtx.taskKeys.addAll(this.taskKeys);
        childCtx.variableNames.addAll(this.variableNames);
        for (WorkflowModelDesignBranchRequest br : branches) {
            childCtx.validateChildNodes(br.getNodes());
        }
        this.taskKeys.addAll(childCtx.taskKeys);
        this.variableNames.addAll(childCtx.variableNames);
    }

    /** 共享 key/var 集合的分支子上下文会回写至父 — 由 validateBranchChildren 处理 */
    public void forEachLeafRequiringRule(List<WorkflowModelDesignNodeRequest> nodes,
                                         Consumer<WorkflowModelDesignNodeRequest> consumer) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (WorkflowModelDesignNodeRequest node : nodes) {
            DesignNodeHandler handler = registry.require(node.getNodeType());
            if (handler.requiresApproverRule()) {
                consumer.accept(node);
            }
            if (!CollectionUtils.isEmpty(node.getBranches())) {
                for (WorkflowModelDesignBranchRequest br : node.getBranches()) {
                    forEachLeafRequiringRule(br.getNodes(), consumer);
                }
            }
        }
    }
}
