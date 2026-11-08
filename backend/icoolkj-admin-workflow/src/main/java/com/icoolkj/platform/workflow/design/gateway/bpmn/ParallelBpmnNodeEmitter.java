package com.icoolkj.platform.workflow.design.gateway.bpmn;

import com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds;
import com.icoolkj.platform.workflow.design.bpmn.BpmnModelElements;
import com.icoolkj.platform.workflow.design.bpmn.BpmnNodeEmitter;
import com.icoolkj.platform.workflow.design.bpmn.BpmnModelEmitContext;
import com.icoolkj.platform.workflow.design.bpmn.BpmnTechnicalNames;
import com.icoolkj.platform.workflow.design.gateway.GatewayKind;
import com.icoolkj.platform.workflow.design.gateway.branch.ParallelBranchSupport;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder.DesignBranch;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder.DesignStep;
import java.util.List;
import org.flowable.bpmn.model.ParallelGateway;
import org.flowable.bpmn.model.Process;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 并行网关 BPMN 发射 */
@Component
public class ParallelBpmnNodeEmitter implements BpmnNodeEmitter {

    @Override
    public String nodeType() {
        return GatewayKind.PARALLEL.nodeType();
    }

    @Override
    public void appendElements(Process process, DesignStep step, BpmnModelEmitContext context) {
        List<DesignBranch> branches = ParallelBranchSupport.resolveBranches(step);
        String key = step.taskDefinitionKey();
        String displayName = StringUtils.hasText(step.name()) ? step.name() : key;

        ParallelGateway fork = new ParallelGateway();
        fork.setId(BpmnElementIds.parallelForkId(key));
        fork.setName(displayName);
        process.addFlowElement(fork);

        for (DesignBranch branch : branches) {
            for (DesignStep child : branch.stepsOrEmpty()) {
                context.appendElements(child);
            }
        }

        ParallelGateway join = new ParallelGateway();
        join.setId(BpmnElementIds.parallelJoinId(key));
        join.setName(BpmnTechnicalNames.PARALLEL_JOIN);
        process.addFlowElement(join);
    }

    @Override
    public void appendFlows(Process process, DesignStep step, String nextTarget, BpmnModelEmitContext context) {
        List<DesignBranch> branches = ParallelBranchSupport.resolveBranches(step);
        String key = step.taskDefinitionKey();
        String forkId = BpmnElementIds.parallelForkId(key);
        String joinId = BpmnElementIds.parallelJoinId(key);
        int idx = 0;
        for (DesignBranch branch : branches) {
            List<DesignStep> kids = branch.stepsOrEmpty();
            String firstTarget = kids.isEmpty() ? joinId : context.entryId(kids.get(0));
            process.addFlowElement(BpmnModelElements.flow(
                    BpmnElementIds.parallelBranchFlowId(key, idx++), forkId, firstTarget));
            for (int i = 0; i < kids.size(); i++) {
                String childNext = (i + 1 < kids.size())
                        ? context.entryId(kids.get(i + 1))
                        : joinId;
                context.appendFlows(kids.get(i), childNext);
            }
        }
        process.addFlowElement(BpmnModelElements.flow(
                BpmnElementIds.parallelJoinOutFlowId(key), joinId, nextTarget));
    }

    @Override
    public String entryId(DesignStep step) {
        return BpmnElementIds.parallelForkId(step.taskDefinitionKey());
    }
}
