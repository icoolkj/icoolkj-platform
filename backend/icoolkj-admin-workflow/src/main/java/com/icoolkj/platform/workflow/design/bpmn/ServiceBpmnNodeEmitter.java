package com.icoolkj.platform.workflow.design.bpmn;

import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import com.icoolkj.platform.workflow.support.WorkflowStrategyConfigSupport;
import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ServiceTask;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ServiceBpmnNodeEmitter implements BpmnNodeEmitter {

    private final WorkflowStrategyConfigSupport strategyConfigSupport;

    public ServiceBpmnNodeEmitter(WorkflowStrategyConfigSupport strategyConfigSupport) {
        this.strategyConfigSupport = strategyConfigSupport;
    }

    @Override
    public String nodeType() {
        return DesignNodeTypes.SERVICE;
    }

    @Override
    public void appendElements(Process process, LinearBpmnBuilder.DesignStep step, BpmnModelEmitContext context) {
        String key = step.taskDefinitionKey();
        String delegateExpression = strategyConfigSupport.requireDelegateExpression(step.strategyConfig(), key);
        ServiceTask task = new ServiceTask();
        task.setId(key);
        task.setName(StringUtils.hasText(step.name()) ? step.name() : key);
        task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
        task.setImplementation(delegateExpression);
        process.addFlowElement(task);
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
