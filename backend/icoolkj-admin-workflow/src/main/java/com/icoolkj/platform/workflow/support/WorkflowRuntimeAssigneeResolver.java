package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds;
import com.icoolkj.platform.workflow.domain.WfApproverRuleEntity;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import com.icoolkj.platform.workflow.mapper.WfApproverRuleMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 节点到达时按审批人规则解析办理人（条件/并行未走到的分支不解析）。
 * <p>办理人必须通过 {@link TaskService#setAssignee} 写入，{@link DelegateTask#setAssignee}
 * 只更新运行时表、不写 {@code ACT_HI_TASKINST}，会导致「我已处理」查不到。
 * <p>抄送：{@code behavior.autoDedup=true} 时同实例已抄送用户不再派发；无人可抄则自动办结直通。
 */
@Component
public class WorkflowRuntimeAssigneeResolver {

    private final WorkflowApproverRuleSupport approverRuleSupport;
    private final WorkflowBizProcessLookup bizProcessLookup;
    private final WfApproverRuleMapper approverRuleMapper;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final WorkflowVersionConfigLookup versionConfigLookup;

    public WorkflowRuntimeAssigneeResolver(WorkflowApproverRuleSupport approverRuleSupport,
                                           WorkflowBizProcessLookup bizProcessLookup,
                                           WfApproverRuleMapper approverRuleMapper,
                                           @Lazy TaskService taskService,
                                           @Lazy HistoryService historyService,
                                           WorkflowVersionConfigLookup versionConfigLookup) {
        this.approverRuleSupport = approverRuleSupport;
        this.bizProcessLookup = bizProcessLookup;
        this.approverRuleMapper = approverRuleMapper;
        this.taskService = taskService;
        this.historyService = historyService;
        this.versionConfigLookup = versionConfigLookup;
    }

    public void resolveForSingleTask(DelegateTask task) {
        if (task == null || !StringUtils.hasText(task.getId())) {
            return;
        }
        boolean cc = WorkflowCcTaskSupport.isCcTask(task)
                || WorkflowCcTaskSupport.isCcTaskKey(task.getTaskDefinitionKey());
        // 会签子任务已由元素变量赋值：仍走 TaskService 同步历史（抄送需可去重覆盖）
        if (!cc && StringUtils.hasText(task.getAssignee())) {
            taskService.setAssignee(task.getId(), task.getAssignee());
            return;
        }
        String taskKey = task.getTaskDefinitionKey();
        Context ctx = resolveContext(task.getProcessInstanceId(), task.getVariable(WorkflowVariables.MODEL_ID),
                task.getVariable(WorkflowVariables.TENANT_ID),
                task.getVariable(WorkflowVariables.BIZ_TYPE),
                task.getVariable(WorkflowVariables.STARTER_USER_ID));
        Map<String, Object> vars = task.getVariables() == null
                ? Collections.emptyMap()
                : task.getVariables();
        List<String> userIds = new ArrayList<>(approverRuleSupport.resolveUserIdsForTask(
                ctx.modelId, taskKey, ctx.tenantId, ctx.bizType, ctx.starterUserId, vars));

        if (cc && isAutoDedup(task.getProcessInstanceId(), ctx.modelId)) {
            Set<String> alreadyCc = loadCcAssignees(task.getProcessInstanceId(), task.getId());
            userIds = userIds.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .filter(u -> !alreadyCc.contains(u))
                    .distinct()
                    .toList();
            if (userIds.isEmpty()) {
                // 无人可抄：直通下一节点，不阻塞主链
                taskService.complete(task.getId());
                return;
            }
        }

        if (CollectionUtils.isEmpty(userIds) || !StringUtils.hasText(userIds.get(0))) {
            throw new BusinessException("error.workflow.approver_not_found", taskKey, "empty");
        }
        String assignee = userIds.get(0).trim();
        task.setVariable(variableNameFor(ctx.modelId, taskKey, ctx.tenantId), assignee);
        task.setVariable(BpmnElementIds.assigneeListVar(taskKey), List.copyOf(userIds));
        taskService.setAssignee(task.getId(), assignee);
    }

    public void resolveForMultiInstanceStart(DelegateExecution execution) {
        if (execution == null) {
            return;
        }
        String taskKey = execution.getCurrentActivityId();
        if (!StringUtils.hasText(taskKey)) {
            return;
        }
        Context ctx = resolveContext(execution.getProcessInstanceId(),
                execution.getVariable(WorkflowVariables.MODEL_ID),
                execution.getVariable(WorkflowVariables.TENANT_ID),
                execution.getVariable(WorkflowVariables.BIZ_TYPE),
                execution.getVariable(WorkflowVariables.STARTER_USER_ID));
        Map<String, Object> vars = execution.getVariables() == null
                ? Collections.emptyMap()
                : execution.getVariables();
        List<String> userIds = new ArrayList<>(approverRuleSupport.resolveUserIdsForTask(
                ctx.modelId, taskKey, ctx.tenantId, ctx.bizType, ctx.starterUserId, vars));
        boolean cc = WorkflowCcTaskSupport.isCcTaskKey(taskKey);
        if (cc && isAutoDedup(execution.getProcessInstanceId(), ctx.modelId)) {
            Set<String> alreadyCc = loadCcAssignees(execution.getProcessInstanceId(), null);
            userIds = userIds.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .filter(u -> !alreadyCc.contains(u))
                    .distinct()
                    .toList();
        }
        String listVar = BpmnElementIds.assigneeListVar(taskKey);
        String varName = variableNameFor(ctx.modelId, taskKey, ctx.tenantId);
        execution.setVariable(listVar, List.copyOf(userIds));
        if (!CollectionUtils.isEmpty(userIds) && StringUtils.hasText(userIds.get(0))) {
            execution.setVariable(varName, userIds.get(0).trim());
        }
    }

    private boolean isAutoDedup(String processInstanceId, String modelId) {
        if (!StringUtils.hasText(processInstanceId) || !StringUtils.hasText(modelId)) {
            return false;
        }
        return versionConfigLookup.resolveByProcessInstanceId(processInstanceId, modelId)
                .behavior()
                .isAutoDedup();
    }

    /** 本实例已出现过的抄送办理人（历史+其他活跃 CC），不含当前任务。 */
    private Set<String> loadCcAssignees(String processInstanceId, String excludeTaskId) {
        Set<String> assignees = new LinkedHashSet<>();
        if (!StringUtils.hasText(processInstanceId)) {
            return assignees;
        }
        List<HistoricTaskInstance> historic = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        if (historic != null) {
            for (HistoricTaskInstance ht : historic) {
                if (ht == null || !WorkflowCcTaskSupport.isCcTask(ht)) {
                    continue;
                }
                if (StringUtils.hasText(excludeTaskId) && excludeTaskId.equals(ht.getId())) {
                    continue;
                }
                if (StringUtils.hasText(ht.getAssignee())) {
                    assignees.add(ht.getAssignee().trim());
                }
            }
        }
        List<Task> active = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
        if (active != null) {
            for (Task t : active) {
                if (t == null || !WorkflowCcTaskSupport.isCcTask(t)) {
                    continue;
                }
                if (StringUtils.hasText(excludeTaskId) && excludeTaskId.equals(t.getId())) {
                    continue;
                }
                if (StringUtils.hasText(t.getAssignee())) {
                    assignees.add(t.getAssignee().trim());
                }
            }
        }
        return assignees;
    }

    private String variableNameFor(String modelId, String taskKey, String bizTenantId) {
        List<WfApproverRuleEntity> rules = approverRuleMapper.selectByModelAndTenants(
                modelId,
                StringUtils.hasText(bizTenantId)
                        ? List.of(com.icoolkj.platform.common.constant.TenantConstants.PLATFORM_TENANT_ID, bizTenantId)
                        : List.of(com.icoolkj.platform.common.constant.TenantConstants.PLATFORM_TENANT_ID));
        if (!CollectionUtils.isEmpty(rules)) {
            for (WfApproverRuleEntity rule : rules) {
                if (taskKey.equals(rule.getTaskDefinitionKey()) && StringUtils.hasText(rule.getVariableName())) {
                    if (StringUtils.hasText(bizTenantId) && bizTenantId.equals(rule.getTenantId())) {
                        return rule.getVariableName().trim();
                    }
                }
            }
            for (WfApproverRuleEntity rule : rules) {
                if (taskKey.equals(rule.getTaskDefinitionKey()) && StringUtils.hasText(rule.getVariableName())) {
                    return rule.getVariableName().trim();
                }
            }
        }
        return taskKey;
    }

    private Context resolveContext(String processInstanceId,
                                   Object modelIdVar,
                                   Object tenantIdVar,
                                   Object bizTypeVar,
                                   Object starterVar) {
        String modelId = asText(modelIdVar);
        String tenantId = asText(tenantIdVar);
        String bizType = asText(bizTypeVar);
        String starterUserId = asText(starterVar);
        if ((!StringUtils.hasText(modelId) || !StringUtils.hasText(bizType))
                && StringUtils.hasText(processInstanceId)) {
            WfBizProcessEntity biz = bizProcessLookup.findByProcessInstanceId(processInstanceId);
            if (biz != null) {
                if (!StringUtils.hasText(modelId)) {
                    modelId = biz.getModelId();
                }
                if (!StringUtils.hasText(tenantId)) {
                    tenantId = biz.getTenantId();
                }
                if (!StringUtils.hasText(bizType)) {
                    bizType = biz.getBizType();
                }
                if (!StringUtils.hasText(starterUserId)) {
                    starterUserId = biz.getStarterUserId();
                }
            }
        }
        if (!StringUtils.hasText(modelId)) {
            throw new BusinessException("error.workflow.approver_not_found", "-", "missing_model");
        }
        return new Context(modelId, tenantId, bizType, starterUserId);
    }

    private static String asText(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private record Context(String modelId, String tenantId, String bizType, String starterUserId) {
    }
}
