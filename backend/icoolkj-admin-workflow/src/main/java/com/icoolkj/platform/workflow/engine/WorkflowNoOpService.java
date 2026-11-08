package com.icoolkj.platform.workflow.engine;

import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 设计器默认 service 节点占位 Bean（delegateExpression：{@code ${workflowNoOpService.execute(execution)}}）。
 * 业务侧应替换为真实 Bean，或在设计器填写自定义表达式。
 */
@Component("workflowNoOpService")
public class WorkflowNoOpService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowNoOpService.class);

    public void execute(DelegateExecution execution) {
        if (log.isDebugEnabled()) {
            log.debug("workflowNoOpService skipped activityId={} processInstanceId={}",
                    execution.getCurrentActivityId(), execution.getProcessInstanceId());
        }
    }
}
