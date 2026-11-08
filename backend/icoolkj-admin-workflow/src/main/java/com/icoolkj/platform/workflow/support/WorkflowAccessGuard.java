package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.PermissionAuthorizationService;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.workflow.constant.WfBizProcessStatus;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 流程实例访问与操作权限校验 */
@Component
public class WorkflowAccessGuard {

    private final TaskService taskService;
    private final HistoryService historyService;
    private final PermissionAuthorizationService authz;

    public WorkflowAccessGuard(TaskService taskService,
                               HistoryService historyService,
                               PermissionAuthorizationService authz) {
        this.taskService = taskService;
        this.historyService = historyService;
        this.authz = authz;
    }

    public void assertCanViewInstance(String userId, WfBizProcessEntity bizProcess) {
        if (bizProcess == null) {
            throw new BusinessException("error.workflow.instance_not_found");
        }
        if (userId.equals(bizProcess.getStarterUserId())) {
            return;
        }
        if (hasAssignedTask(userId, bizProcess.getProcessInstanceId())) {
            return;
        }
        if (canMonitorTenant(bizProcess)) {
            return;
        }
        throw new BusinessException("error.workflow.instance_access_denied");
    }

    public void assertCanCancel(String userId, WfBizProcessEntity bizProcess) {
        if (bizProcess == null) {
            throw new BusinessException("error.workflow.instance_not_found");
        }
        if (!userId.equals(bizProcess.getStarterUserId())) {
            throw new BusinessException("error.workflow.not_starter");
        }
        String status = bizProcess.getStatus();
        if (!WfBizProcessStatus.RUNNING.equals(status) && !WfBizProcessStatus.RETURNED.equals(status)) {
            throw new BusinessException("error.workflow.instance_not_running");
        }
    }

    /** 租户管理员终止运行中/退回中实例（流程监控） */
    public void assertCanTerminate(String userId, WfBizProcessEntity bizProcess) {
        if (bizProcess == null) {
            throw new BusinessException("error.workflow.instance_not_found");
        }
        if (!canMonitorTenant(bizProcess)) {
            throw new BusinessException("error.workflow.instance_access_denied");
        }
        String status = bizProcess.getStatus();
        if (!WfBizProcessStatus.RUNNING.equals(status) && !WfBizProcessStatus.RETURNED.equals(status)) {
            throw new BusinessException("error.workflow.instance_not_running");
        }
    }

    /** 租户流程监控：本租户管理员查看任意实例 */
    public boolean canMonitorTenant(WfBizProcessEntity bizProcess) {
        if (bizProcess == null || !StringUtils.hasText(bizProcess.getTenantId())) {
            return false;
        }
        if (!authz.hasAnyPermission("workflow:monitor:list", "workflow:monitor:view")) {
            return false;
        }
        String tenantId = SecurityContextHelper.getTenantId();
        return StringUtils.hasText(tenantId) && tenantId.equals(bizProcess.getTenantId());
    }

    private boolean hasAssignedTask(String userId, String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return false;
        }
        Task active = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee(userId)
                .active()
                .singleResult();
        if (active != null) {
            return true;
        }
        Long historicCount = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee(userId)
                .finished()
                .count();
        return historicCount != null && historicCount > 0;
    }
}
