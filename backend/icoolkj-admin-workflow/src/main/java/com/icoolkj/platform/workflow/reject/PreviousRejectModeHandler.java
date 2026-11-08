package com.icoolkj.platform.workflow.reject;

import com.icoolkj.platform.workflow.support.WorkflowAssigneeTaskListener;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PreviousRejectModeHandler extends AbstractRejectJumpHandler {

    @Override
    public String rejectMode() {
        return "previous";
    }

    @Override
    protected String resolveTargetActivityId(Task task, RejectModeContext context) {
        Object prev = context.runtimeService().getVariable(
                task.getProcessInstanceId(), WorkflowAssigneeTaskListener.REJECT_PREVIOUS_KEY);
        if (prev != null && StringUtils.hasText(String.valueOf(prev))) {
            return String.valueOf(prev).trim();
        }
        return null;
    }
}
