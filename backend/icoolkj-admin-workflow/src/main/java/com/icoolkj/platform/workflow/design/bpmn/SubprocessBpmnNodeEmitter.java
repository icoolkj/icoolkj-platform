package com.icoolkj.platform.workflow.design.bpmn;

import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import com.icoolkj.platform.workflow.support.WorkflowStrategyConfigSupport;
import org.flowable.bpmn.model.CallActivity;
import org.flowable.bpmn.model.Process;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SubprocessBpmnNodeEmitter implements BpmnNodeEmitter {

    private final WorkflowStrategyConfigSupport strategyConfigSupport;

    public SubprocessBpmnNodeEmitter(WorkflowStrategyConfigSupport strategyConfigSupport) {
        this.strategyConfigSupport = strategyConfigSupport;
    }

    @Override
    public String nodeType() {
        return DesignNodeTypes.SUBPROCESS;
    }

    @Override
    public void appendElements(Process process, LinearBpmnBuilder.DesignStep step, BpmnModelEmitContext context) {
        String key = step.taskDefinitionKey();
        String calledElement = strategyConfigSupport.requireCalledElement(step.strategyConfig(), key);
        CallActivity call = new CallActivity();
        call.setId(key);
        call.setName(StringUtils.hasText(step.name()) ? step.name() : key);
        call.setCalledElement(calledElement);
        call.setInheritVariables(true);
        process.addFlowElement(call);
    }

    @Override
    public void appendFlows(Process process, LinearBpmnBuilder.DesignStep step, String nextTarget,
                            BpmnModelEmitContext context) {
        String key = step.taskDefinitionKey();
        process.addFlowElement(BpmnModelElements.flow(BpmnElementIds.flowFromTask(key), key, nextTarget));
    }

    @Override
    public String entryId(LinearBpmnBuilder.DesignStep step) {
        return step.taskDefinitionKey();
    }
}
