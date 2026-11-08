package com.icoolkj.platform.workflow.reject;

import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StarterRejectModeHandler extends AbstractRejectJumpHandler {

    @Override
    public String rejectMode() {
        return "starter";
    }

    @Override
    protected String resolveTargetActivityId(Task task, RejectModeContext context) {
        Object first = context.runtimeService().getVariable(
                task.getProcessInstanceId(), WorkflowVariables.FIRST_USER_TASK_KEY);
        if (first != null && StringUtils.hasText(String.valueOf(first))) {
            return String.valueOf(first).trim();
        }
        return null;
    }
}
