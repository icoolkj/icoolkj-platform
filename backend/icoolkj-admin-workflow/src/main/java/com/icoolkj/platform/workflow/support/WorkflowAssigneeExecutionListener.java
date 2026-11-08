package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * userTask start：解析办理人变量（单人 / 会签共用）；维护驳回跳转轨迹。
 * 单人节点配合 BPMN assignee 表达式，保证历史表也能落到办理人。
 */
@Component(BpmnElementIds.BEAN_ASSIGNEE_EXECUTION_LISTENER)
public class WorkflowAssigneeExecutionListener implements ExecutionListener {

    private final WorkflowRuntimeAssigneeResolver runtimeAssigneeResolver;

    public WorkflowAssigneeExecutionListener(WorkflowRuntimeAssigneeResolver runtimeAssigneeResolver) {
        this.runtimeAssigneeResolver = runtimeAssigneeResolver;
    }

    @Override
    public void notify(DelegateExecution execution) {
        String key = execution.getCurrentActivityId();
        // 抄送节点不写入驳回轨迹，避免 previous/starter 跳到抄送
        if (StringUtils.hasText(key) && !WorkflowCcTaskSupport.isCcTaskKey(key)) {
            Object first = execution.getVariable(WorkflowVariables.FIRST_USER_TASK_KEY);
            if (first == null || !StringUtils.hasText(String.valueOf(first))) {
                execution.setVariable(WorkflowVariables.FIRST_USER_TASK_KEY, key);
            }
            Object last = execution.getVariable(WorkflowVariables.LAST_USER_TASK_KEY);
            if (last != null && StringUtils.hasText(String.valueOf(last))) {
                String prev = String.valueOf(last).trim();
                if (!key.equals(prev)) {
                    execution.setVariable(WorkflowAssigneeTaskListener.REJECT_PREVIOUS_KEY, prev);
                }
            }
            execution.setVariable(WorkflowVariables.LAST_USER_TASK_KEY, key);
        }
        runtimeAssigneeResolver.resolveForMultiInstanceStart(execution);
    }
}
