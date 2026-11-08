package com.icoolkj.platform.workflow.design.gateway;

import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds;
import com.icoolkj.platform.workflow.design.bpmn.BpmnTechnicalNames;
import com.icoolkj.platform.workflow.design.gateway.bpmn.ConditionLikeBpmnIds;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 设计器网关种类（与 design JSON {@code nodeType} 对齐）。
 */
public enum GatewayKind {

    CONDITION(DesignNodeTypes.CONDITION),
    INCLUSIVE(DesignNodeTypes.INCLUSIVE),
    PARALLEL(DesignNodeTypes.PARALLEL);

    private final String nodeType;

    GatewayKind(String nodeType) {
        this.nodeType = nodeType;
    }

    public String nodeType() {
        return nodeType;
    }

    public boolean isConditionLike() {
        return this == CONDITION || this == INCLUSIVE;
    }

    public boolean isExclusive() {
        return this == CONDITION;
    }

    public ConditionLikeBpmnIds conditionLikeBpmnIds() {
        if (!isConditionLike()) {
            throw new IllegalStateException("Not a condition-like gateway: " + this);
        }
        return switch (this) {
            case CONDITION -> conditionLikeIds(
                    BpmnElementIds::conditionSplitId,
                    BpmnElementIds::conditionMergeId,
                    BpmnElementIds::conditionDefaultFlowId,
                    BpmnElementIds::conditionBranchFlowId,
                    BpmnElementIds::conditionMergeOutFlowId,
                    BpmnTechnicalNames.MERGE);
            case INCLUSIVE -> conditionLikeIds(
                    BpmnElementIds::inclusiveSplitId,
                    BpmnElementIds::inclusiveMergeId,
                    BpmnElementIds::inclusiveDefaultFlowId,
                    BpmnElementIds::inclusiveBranchFlowId,
                    BpmnElementIds::inclusiveMergeOutFlowId,
                    BpmnTechnicalNames.INCLUSIVE_JOIN);
            default -> throw new IllegalStateException("Not a condition-like gateway: " + this);
        };
    }

    private static ConditionLikeBpmnIds conditionLikeIds(
            Function<String, String> splitId,
            Function<String, String> mergeId,
            Function<String, String> defaultFlowId,
            BiFunction<String, String, String> branchFlowId,
            Function<String, String> mergeOutFlowId,
            String mergeTechnicalName) {
        return new ConditionLikeBpmnIds(splitId, mergeId, defaultFlowId, branchFlowId, mergeOutFlowId,
                mergeTechnicalName);
    }
}
