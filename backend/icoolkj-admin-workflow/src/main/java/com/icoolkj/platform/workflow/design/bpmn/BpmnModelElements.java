package com.icoolkj.platform.workflow.design.bpmn;

import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.SequenceFlow;

/** Flowable Model 构造小工具（design.bpmn 与 design.gateway.bpmn 共用） */
public final class BpmnModelElements {

    private BpmnModelElements() {
    }

    public static FlowableListener taskCreateListener(String delegateExpression) {
        FlowableListener listener = new FlowableListener();
        listener.setEvent("create");
        listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
        listener.setImplementation(delegateExpression);
        return listener;
    }

    public static FlowableListener executionStartListener(String delegateExpression) {
        FlowableListener listener = new FlowableListener();
        listener.setEvent("start");
        listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
        listener.setImplementation(delegateExpression);
        return listener;
    }

    public static FlowableListener executionEndListener(String delegateExpression) {
        FlowableListener listener = new FlowableListener();
        listener.setEvent("end");
        listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
        listener.setImplementation(delegateExpression);
        return listener;
    }

    public static SequenceFlow flow(String id, String source, String target) {
        SequenceFlow flow = new SequenceFlow();
        flow.setId(id);
        flow.setSourceRef(source);
        flow.setTargetRef(target);
        return flow;
    }

    public static SequenceFlow flowWithCondition(String id, String source, String target, String expression) {
        SequenceFlow flow = flow(id, source, target);
        flow.setConditionExpression(expression);
        return flow;
    }
}
