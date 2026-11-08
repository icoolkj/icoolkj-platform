package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds;
import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import com.icoolkj.platform.workflow.design.gateway.branch.ConditionLikeBranchSupport;
import com.icoolkj.platform.workflow.design.gateway.branch.ParallelBranchSupport;
import java.util.List;
import java.util.Locale;
import org.springframework.util.StringUtils;

/**
 * 设计步骤模型（DesignStep）与分支补齐逻辑。
 * <p>BPMN 元素 id / 连线 / 变量名见 {@link com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds}；
 * 生成入口见 {@code BpmnNodeEmitterRegistry}。
 */
public final class LinearBpmnBuilder {

    /** @deprecated use {@link DesignNodeTypes#APPROVE} */
    public static final String NODE_APPROVE = DesignNodeTypes.APPROVE;
    /** @deprecated use {@link DesignNodeTypes#CC} */
    public static final String NODE_CC = DesignNodeTypes.CC;
    /** @deprecated use {@link DesignNodeTypes#CONDITION} */
    public static final String NODE_CONDITION = DesignNodeTypes.CONDITION;
    /** @deprecated use {@link DesignNodeTypes#PARALLEL} */
    public static final String NODE_PARALLEL = DesignNodeTypes.PARALLEL;
    /** @deprecated use {@link DesignNodeTypes#INCLUSIVE} */
    public static final String NODE_INCLUSIVE = DesignNodeTypes.INCLUSIVE;

    public static final String PASS_SEQUENTIAL = "sequential";
    public static final String PASS_ANY = "any";
    public static final String PASS_ALL = "all";
    public static final String PASS_SEQUENTIAL_ALL = "sequentialAll";

    private LinearBpmnBuilder() {
    }

    /** 审批节点（兼容旧调用） */
    public record ApproveNode(String taskDefinitionKey, String name, String variableName) {
    }

    /** 网关一条路径 */
    public record DesignBranch(String id, String name, String expression, boolean isDefault,
                               List<DesignStep> steps) {
        public List<DesignStep> stepsOrEmpty() {
            return steps == null ? List.of() : steps;
        }
    }

    /** 设计步骤 */
    public record DesignStep(String nodeType, String taskDefinitionKey, String name,
                             String variableName, String conditionExpression,
                             List<DesignBranch> branches, String passMode,
                             String dueDuration, String timeoutAction,
                             String strategyConfig) {
        public DesignStep(String nodeType, String taskDefinitionKey, String name,
                          String variableName, String conditionExpression) {
            this(nodeType, taskDefinitionKey, name, variableName, conditionExpression, null, null, null, null, null);
        }

        public DesignStep(String nodeType, String taskDefinitionKey, String name,
                          String variableName, String conditionExpression,
                          List<DesignBranch> branches) {
            this(nodeType, taskDefinitionKey, name, variableName, conditionExpression, branches, null, null, null, null);
        }

        public DesignStep(String nodeType, String taskDefinitionKey, String name,
                          String variableName, String conditionExpression,
                          List<DesignBranch> branches, String passMode) {
            this(nodeType, taskDefinitionKey, name, variableName, conditionExpression, branches, passMode, null, null, null);
        }

        public DesignStep(String nodeType, String taskDefinitionKey, String name,
                          String variableName, String conditionExpression,
                          List<DesignBranch> branches, String passMode,
                          String dueDuration, String timeoutAction) {
            this(nodeType, taskDefinitionKey, name, variableName, conditionExpression, branches, passMode,
                    dueDuration, timeoutAction, null);
        }

        public static DesignStep fromApprove(ApproveNode node) {
            return new DesignStep(NODE_APPROVE, node.taskDefinitionKey(), node.name(),
                    node.variableName(), null, null, null, null, null, null);
        }

        public String normalizedType() {
            if (!StringUtils.hasText(nodeType)) {
                return NODE_APPROVE;
            }
            return nodeType.trim().toLowerCase(Locale.ROOT);
        }

        public List<DesignBranch> branchesOrEmpty() {
            return branches == null ? List.of() : branches;
        }

        public String normalizedPassMode() {
            if (!StringUtils.hasText(passMode)) {
                return PASS_SEQUENTIAL;
            }
            String v = passMode.trim().toLowerCase(Locale.ROOT);
            if (PASS_ANY.equals(v)) {
                return PASS_ANY;
            }
            if (PASS_ALL.equals(v)) {
                return PASS_ALL;
            }
            if ("sequentialall".equals(v)) {
                return PASS_SEQUENTIAL_ALL;
            }
            return PASS_SEQUENTIAL;
        }

        public boolean isMultiInstance() {
            String m = normalizedPassMode();
            return PASS_ANY.equals(m) || PASS_ALL.equals(m) || PASS_SEQUENTIAL_ALL.equals(m);
        }
    }

    /** @see com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds#assigneeListVar(String) */
    public static String assigneeListVarName(String taskDefinitionKey) {
        return BpmnElementIds.assigneeListVar(taskDefinitionKey);
    }

    public static List<DesignBranch> resolveConditionBranches(DesignStep step) {
        return ConditionLikeBranchSupport.resolveBranches(step);
    }

    public static List<DesignBranch> resolveInclusiveBranches(DesignStep step) {
        return ConditionLikeBranchSupport.resolveBranches(step);
    }

    public static List<DesignBranch> resolveParallelBranches(DesignStep step) {
        return ParallelBranchSupport.resolveBranches(step);
    }
}
