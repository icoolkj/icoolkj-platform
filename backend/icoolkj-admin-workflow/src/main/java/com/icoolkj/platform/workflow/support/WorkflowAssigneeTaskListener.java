package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** userTask create：单人审批/抄送解析办理人；维护驳回跳转用的首/末节点变量 */
@Component(BpmnElementIds.BEAN_ASSIGNEE_TASK_LISTENER)
public class WorkflowAssigneeTaskListener implements TaskListener {

    /** 进入当前节点前一审批节点（供 rejectMode=previous） */
    public static final String REJECT_PREVIOUS_KEY = "wfRejectPreviousKey";

    private final WorkflowRuntimeAssigneeResolver runtimeAssigneeResolver;

    public WorkflowAssigneeTaskListener(WorkflowRuntimeAssigneeResolver runtimeAssigneeResolver) {
        this.runtimeAssigneeResolver = runtimeAssigneeResolver;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        if (!WorkflowCcTaskSupport.isCcCategory(delegateTask.getCategory())
                && !WorkflowCcTaskSupport.isCcTaskKey(delegateTask.getTaskDefinitionKey())) {
            trackApproveTrail(delegateTask);
        }
        runtimeAssigneeResolver.resolveForSingleTask(delegateTask);
    }

    private void trackApproveTrail(DelegateTask task) {
        String key = task.getTaskDefinitionKey();
        if (!StringUtils.hasText(key)) {
            return;
        }
        Object first = task.getVariable(WorkflowVariables.FIRST_USER_TASK_KEY);
        if (first == null || !StringUtils.hasText(String.valueOf(first))) {
            task.setVariable(WorkflowVariables.FIRST_USER_TASK_KEY, key);
        }
        Object last = task.getVariable(WorkflowVariables.LAST_USER_TASK_KEY);
        if (last != null && StringUtils.hasText(String.valueOf(last))) {
            String prev = String.valueOf(last).trim();
            if (!key.equals(prev)) {
                task.setVariable(REJECT_PREVIOUS_KEY, prev);
            }
        }
        task.setVariable(WorkflowVariables.LAST_USER_TASK_KEY, key);
    }
}
