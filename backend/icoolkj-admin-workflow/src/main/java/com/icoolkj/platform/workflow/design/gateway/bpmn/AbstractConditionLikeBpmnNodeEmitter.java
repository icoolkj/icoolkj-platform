package com.icoolkj.platform.workflow.design.gateway.bpmn;

import com.icoolkj.platform.workflow.design.bpmn.BpmnModelElements;
import com.icoolkj.platform.workflow.design.bpmn.BpmnNodeEmitter;
import com.icoolkj.platform.workflow.design.bpmn.BpmnModelEmitContext;
import com.icoolkj.platform.workflow.design.gateway.GatewayKind;
import com.icoolkj.platform.workflow.design.gateway.branch.ConditionLikeBranchSupport;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder.DesignBranch;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder.DesignStep;
import java.util.List;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.InclusiveGateway;
import org.flowable.bpmn.model.Process;
import org.springframework.util.StringUtils;

/**
 * 排他 / 包容网关 BPMN 发射器基类（分裂 + 汇合 + 条件出线）。
 */
public abstract class AbstractConditionLikeBpmnNodeEmitter implements BpmnNodeEmitter {

    private final GatewayKind kind;
    private final ConditionLikeBpmnIds ids;

    protected AbstractConditionLikeBpmnNodeEmitter(GatewayKind kind) {
        if (!kind.isConditionLike()) {
            throw new IllegalArgumentException("Expected condition-like gateway: " + kind);
        }
        this.kind = kind;
        this.ids = kind.conditionLikeBpmnIds();
    }

    @Override
    public final String nodeType() {
        return kind.nodeType();
    }

    @Override
    public void appendElements(Process process, DesignStep step, BpmnModelEmitContext context) {
        List<DesignBranch> branches = ConditionLikeBranchSupport.resolveBranches(step);
        String key = step.taskDefinitionKey();
        String displayName = StringUtils.hasText(step.name()) ? step.name() : key;
        String gwId = ids.splitId().apply(key);
        String mergeId = ids.mergeId().apply(key);

        FlowElement split = newSplitGateway(gwId, displayName, ids.defaultFlowId().apply(key));
        process.addFlowElement(split);

        for (DesignBranch branch : branches) {
            for (DesignStep child : branch.stepsOrEmpty()) {
                context.appendElements(child);
            }
        }

        FlowElement merge = newMergeGateway(mergeId, ids.mergeTechnicalName());
        process.addFlowElement(merge);
    }

    @Override
    public void appendFlows(Process process, DesignStep step, String nextTarget, BpmnModelEmitContext context) {
        List<DesignBranch> branches = ConditionLikeBranchSupport.resolveBranches(step);
        String key = step.taskDefinitionKey();
        String gwId = ids.splitId().apply(key);
        String mergeId = ids.mergeId().apply(key);

        for (DesignBranch branch : branches) {
            List<DesignStep> kids = branch.stepsOrEmpty();
            String firstTarget = kids.isEmpty() ? mergeId : context.entryId(kids.get(0));
            if (branch.isDefault()) {
                process.addFlowElement(BpmnModelElements.flow(
                        ids.defaultFlowId().apply(key), gwId, firstTarget));
            } else {
                String expr = StringUtils.hasText(branch.expression())
                        ? branch.expression().trim()
                        : (StringUtils.hasText(step.conditionExpression())
                        ? step.conditionExpression().trim() : "true");
                process.addFlowElement(BpmnModelElements.flowWithCondition(
                        ids.branchFlowId().apply(key, branch.id()), gwId, firstTarget, expr));
            }
            for (int i = 0; i < kids.size(); i++) {
                String childNext = (i + 1 < kids.size())
                        ? context.entryId(kids.get(i + 1))
                        : mergeId;
                context.appendFlows(kids.get(i), childNext);
            }
        }
        process.addFlowElement(BpmnModelElements.flow(
                ids.mergeOutFlowId().apply(key), mergeId, nextTarget));
    }

    @Override
    public String entryId(DesignStep step) {
        return ids.splitId().apply(step.taskDefinitionKey());
    }

    private FlowElement newSplitGateway(String id, String name, String defaultFlowId) {
        if (kind == GatewayKind.CONDITION) {
            ExclusiveGateway split = new ExclusiveGateway();
            split.setId(id);
            split.setName(name);
            split.setDefaultFlow(defaultFlowId);
            return split;
        }
        InclusiveGateway split = new InclusiveGateway();
        split.setId(id);
        split.setName(name);
        split.setDefaultFlow(defaultFlowId);
        return split;
    }

    private FlowElement newMergeGateway(String id, String technicalName) {
        if (kind == GatewayKind.CONDITION) {
            ExclusiveGateway merge = new ExclusiveGateway();
            merge.setId(id);
            merge.setName(technicalName);
            return merge;
        }
        InclusiveGateway merge = new InclusiveGateway();
        merge.setId(id);
        merge.setName(technicalName);
        return merge;
    }
}
