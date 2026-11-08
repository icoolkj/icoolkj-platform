package com.icoolkj.platform.workflow.design.bpmn;

import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import com.icoolkj.platform.workflow.support.WorkflowCcTaskSupport;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CcBpmnNodeEmitter implements BpmnNodeEmitter {

    @Override
    public String nodeType() {
        return LinearBpmnBuilder.NODE_CC;
    }

    @Override
    public void appendElements(Process process, LinearBpmnBuilder.DesignStep step, BpmnModelEmitContext context) {
        String key = step.taskDefinitionKey();
        UserTask task = new UserTask();
        task.setId(key);
        task.setName(StringUtils.hasText(step.name()) ? step.name() : key);
        task.setCategory(WorkflowCcTaskSupport.CATEGORY_CC);
        if (StringUtils.hasText(step.variableName())) {
            task.setAssignee(BpmnElementIds.varEl(step.variableName()));
        }
        task.getExecutionListeners().add(
                BpmnModelElements.executionStartListener(BpmnElementIds.assigneeExecutionListenerEl()));
        task.getTaskListeners().add(
                BpmnModelElements.taskCreateListener(BpmnElementIds.assigneeTaskListenerEl()));
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
