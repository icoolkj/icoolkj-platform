package com.icoolkj.platform.workflow.design.bpmn;

import com.icoolkj.platform.workflow.support.LinearBpmnBuilder;
import org.flowable.bpmn.model.Process;

/**
 * 将设计步骤投影为 Flowable {@link Process} 片段（Model API，禁止拼 XML 字符串）。
 */
public interface BpmnNodeEmitter {

    String nodeType();

    void appendElements(Process process, LinearBpmnBuilder.DesignStep step, BpmnModelEmitContext context);

    void appendFlows(Process process, LinearBpmnBuilder.DesignStep step, String nextTarget,
                     BpmnModelEmitContext context);

    String entryId(LinearBpmnBuilder.DesignStep step);
}
