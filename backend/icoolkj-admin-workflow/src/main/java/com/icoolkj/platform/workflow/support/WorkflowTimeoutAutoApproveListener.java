package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 超时自动通过：中断边界触发后写入 {@code approved=true}，再沿边界出线进入下一节点。
 */
@Component(BpmnElementIds.BEAN_TIMEOUT_AUTO_APPROVE_LISTENER)
public class WorkflowTimeoutAutoApproveListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) {
        execution.setVariable(WorkflowVariables.APPROVED, Boolean.TRUE);
    }
}
