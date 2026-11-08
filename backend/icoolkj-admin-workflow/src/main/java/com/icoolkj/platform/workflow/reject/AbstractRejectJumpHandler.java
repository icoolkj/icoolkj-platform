package com.icoolkj.platform.workflow.reject;

import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import org.flowable.task.api.Task;
import org.springframework.util.StringUtils;

/** 驳回跳转公共逻辑：解析目标 → changeState → 审计/回调。 */
abstract class AbstractRejectJumpHandler implements RejectModeHandler {

    @Override
    public boolean handle(Task task, WfBizProcessEntity biz, String comment, RejectModeContext context) {
        String pi = task.getProcessInstanceId();
        String currentKey = task.getTaskDefinitionKey();
        String targetKey = resolveTargetActivityId(task, context);
        if (!StringUtils.hasText(targetKey) || targetKey.equals(currentKey)) {
            return false;
        }
        context.taskService().setVariableLocal(task.getId(), WorkflowVariables.APPROVED, false);
        context.runtimeService().setVariable(pi, WorkflowVariables.APPROVED, false);
        context.runtimeService().createChangeActivityStateBuilder()
                .processInstanceId(pi)
                .moveActivityIdTo(currentKey, targetKey)
                .changeState();
        if (suspendAfterJump()) {
            context.runtimeService().setVariable(pi, WorkflowVariables.STARTER_EDIT_PENDING, true);
            context.runtimeService().suspendProcessInstanceById(pi);
        }
        context.afterRejectJump(task, biz, rejectMode(), targetKey, comment);
        return true;
    }

    /** starterEdit 跳转后挂起 */
    protected boolean suspendAfterJump() {
        return false;
    }

    protected abstract String resolveTargetActivityId(Task task, RejectModeContext context);
}
