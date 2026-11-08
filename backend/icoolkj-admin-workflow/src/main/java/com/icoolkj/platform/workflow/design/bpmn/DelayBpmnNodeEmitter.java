package com.icoolkj.platform.workflow.design.bpmn;

import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import org.flowable.bpmn.model.IntermediateCatchEvent;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.TimerEventDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DelayBpmnNodeEmitter implements BpmnNodeEmitter {

    @Override
    public String nodeType() {
        return DesignNodeTypes.DELAY;
    }

    @Override
    public void appendElements(Process process, LinearBpmnBuilder.DesignStep step, BpmnModelEmitContext context) {
        String key = step.taskDefinitionKey();
        String eventId = BpmnElementIds.delayEventId(key);
        IntermediateCatchEvent event = new IntermediateCatchEvent();
        event.setId(eventId);
        event.setName(StringUtils.hasText(step.name()) ? step.name() : key);
        TimerEventDefinition timer = new TimerEventDefinition();
        timer.setTimeDuration(step.dueDuration());
        event.addEventDefinition(timer);
        process.addFlowElement(event);
    }

    @Override
    public void appendFlows(Process process, LinearBpmnBuilder.DesignStep step, String nextTarget,
                            BpmnModelEmitContext context) {
        String key = step.taskDefinitionKey();
        process.addFlowElement(BpmnModelElements.flow(
                BpmnElementIds.flowFromDelay(key),
                BpmnElementIds.delayEventId(key),
                nextTarget));
    }

    @Override
    public String entryId(LinearBpmnBuilder.DesignStep step) {
        return BpmnElementIds.delayEventId(step.taskDefinitionKey());
    }
}
