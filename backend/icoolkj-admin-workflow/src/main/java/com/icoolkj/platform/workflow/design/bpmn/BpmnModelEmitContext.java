package com.icoolkj.platform.workflow.design.bpmn;

import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import org.flowable.bpmn.model.Process;

/** BPMN Model 发射上下文：向同一 Process 追加元素，支持子树递归 */
public interface BpmnModelEmitContext {

    Process process();

    BpmnNodeEmitter require(String nodeType);

    void appendElements(LinearBpmnBuilder.DesignStep step);

    void appendFlows(LinearBpmnBuilder.DesignStep step, String nextTarget);

    String entryId(LinearBpmnBuilder.DesignStep step);
}
