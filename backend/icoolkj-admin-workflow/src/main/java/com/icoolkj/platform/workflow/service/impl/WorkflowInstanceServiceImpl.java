package com.icoolkj.platform.workflow.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.LocaleListJoin;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.workflow.constant.WfBizProcessStatus;
import com.icoolkj.platform.workflow.constant.WorkflowCancelReasons;
import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import com.icoolkj.platform.workflow.dto.WorkflowInstanceCancelRequest;
import com.icoolkj.platform.workflow.dto.WorkflowInstancePageQuery;
import com.icoolkj.platform.workflow.mapper.WfBizProcessMapper;
import com.icoolkj.platform.workflow.service.WorkflowInstanceService;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import com.icoolkj.platform.workflow.service.WorkflowProcessService;
import com.icoolkj.platform.workflow.spi.WorkflowBizHandler;
import com.icoolkj.platform.workflow.spi.WorkflowUserDisplayNameResolver;
import com.icoolkj.platform.workflow.support.WorkflowAccessGuard;
import com.icoolkj.platform.workflow.support.WorkflowBizProcessLookup;
import com.icoolkj.platform.workflow.support.WorkflowInstanceTimelineBuilder;
import com.icoolkj.platform.workflow.support.WorkflowMappingSupport;
import com.icoolkj.platform.workflow.support.WorkflowProcessConfigSupport;
import com.icoolkj.platform.workflow.support.WorkflowVersionConfigLookup;
import com.icoolkj.platform.workflow.vo.WorkflowInstanceDetailVO;
import com.icoolkj.platform.workflow.vo.WorkflowInstanceVO;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 工作流实例服务 */
@Service
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService {

    private final WorkflowProcessService workflowProcessService;
    private final WfBizProcessMapper wfBizProcessMapper;
    private final WorkflowModelRegistry workflowModelRegistry;
    private final WorkflowAccessGuard workflowAccessGuard;
    private final WorkflowInstanceTimelineBuilder timelineBuilder;
    private final WorkflowBizProcessLookup workflowBizProcessLookup;
    private final WorkflowUserDisplayNameResolver userDisplayNameResolver;
    private final WorkflowVersionConfigLookup versionConfigLookup;
    private final TaskService taskService;
    private final List<WorkflowBizHandler> bizHandlers;

    public WorkflowInstanceServiceImpl(WorkflowProcessService workflowProcessService,
                                       WfBizProcessMapper wfBizProcessMapper,
                                       WorkflowModelRegistry workflowModelRegistry,
                                       WorkflowAccessGuard workflowAccessGuard,
                                       WorkflowInstanceTimelineBuilder timelineBuilder,
                                       WorkflowBizProcessLookup workflowBizProcessLookup,
                                       WorkflowUserDisplayNameResolver userDisplayNameResolver,
                                       WorkflowVersionConfigLookup versionConfigLookup,
                                       TaskService taskService,
                                       List<WorkflowBizHandler> bizHandlers) {
        this.workflowProcessService = workflowProcessService;
        this.wfBizProcessMapper = wfBizProcessMapper;
        this.workflowModelRegistry = workflowModelRegistry;
        this.workflowAccessGuard = workflowAccessGuard;
        this.timelineBuilder = timelineBuilder;
        this.workflowBizProcessLookup = workflowBizProcessLookup;
        this.userDisplayNameResolver = userDisplayNameResolver;
        this.versionConfigLookup = versionConfigLookup;
        this.taskService = taskService;
        this.bizHandlers = bizHandlers == null ? List.of() : bizHandlers;
    }

    @Override
    public PageResult<WorkflowInstanceVO> pageSubmitted(WorkflowInstancePageQuery query) {
        String userId = requireUserId();
        // 已发申请：每业务单据仅最新一轮主流程（多轮历史在详情 processCycles）
        long total = wfBizProcessMapper.countSubmittedByStarter(userId, query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<WorkflowInstanceVO> rows = wfBizProcessMapper.selectSubmittedPageByStarter(userId, query).stream()
                .map(this::toInstanceVo)
                .collect(Collectors.toList());
        fillStarterDisplayNames(rows);
        enrichActiveAssignees(rows);
        enrichAllowWithdraw(rows);
        enrichBizApproveStatus(rows);
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public PageResult<WorkflowInstanceVO> pageTenant(WorkflowInstancePageQuery query) {
        String tenantId = requireTenantId();
        boolean unresolvedOnly = query != null && query.unresolvedOnlyEnabled();
        if (unresolvedOnly) {
            WorkflowInstancePageQuery runningQuery = copyQuery(query);
            runningQuery.setStatus(WfBizProcessStatus.RUNNING);
            runningQuery.setUnresolvedOnly(null);
            List<WfBizProcessEntity> entities = wfBizProcessMapper.selectListByTenant(tenantId, runningQuery);
            List<WorkflowInstanceVO> all = entities.stream().map(this::toInstanceVo).collect(Collectors.toList());
            fillStarterDisplayNames(all);
            enrichActiveAssignees(all);
            enrichAllowWithdraw(all);
            enrichBizApproveStatus(all);
            List<WorkflowInstanceVO> filtered = all.stream()
                    .filter(vo -> Boolean.TRUE.equals(vo.getAssigneeUnresolved()))
                    .collect(Collectors.toList());
            return WorkflowMappingSupport.paginate(filtered, query);
        }

        long total = wfBizProcessMapper.countByTenant(tenantId, query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<WorkflowInstanceVO> rows = wfBizProcessMapper.selectPageByTenant(tenantId, query).stream()
                .map(this::toInstanceVo)
                .collect(Collectors.toList());
        fillStarterDisplayNames(rows);
        enrichActiveAssignees(rows);
        enrichAllowWithdraw(rows);
        enrichBizApproveStatus(rows);
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public WorkflowInstanceDetailVO getByInstanceId(String instanceId) {
        WfBizProcessEntity bizProcess = workflowBizProcessLookup.requireByProcessInstanceId(instanceId);
        String userId = requireUserId();
        workflowAccessGuard.assertCanViewInstance(userId, bizProcess);

        Map<String, Object> variables = WorkflowMappingSupport.toVariableMap(
                workflowProcessService.getProcessVariables(instanceId));

        WorkflowInstanceDetailVO detail = new WorkflowInstanceDetailVO();
        WorkflowInstanceVO base = toInstanceVo(bizProcess);
        copyInstanceFields(detail, base);
        fillStarterDisplayNames(List.of(detail));
        enrichActiveAssignees(List.of(detail));
        if (!StringUtils.hasText(detail.getStarterUserName())) {
            detail.setStarterUserName(WorkflowMappingSupport.asString(
                    variables.get(WorkflowVariables.STARTER_USER_NAME)));
        }
        detail.setVariables(variables);
        timelineBuilder.enrich(detail, instanceId, variables);
        fillRuntimeFlags(detail, bizProcess);
        return detail;
    }

    private void fillRuntimeFlags(WorkflowInstanceDetailVO detail, WfBizProcessEntity bizProcess) {
        if (detail == null || bizProcess == null) {
            return;
        }
        WorkflowProcessConfigSupport.ProcessConfig config = versionConfigLookup.resolveByProcessInstanceId(
                bizProcess.getProcessInstanceId(), bizProcess.getModelId());
        var behavior = config.behavior();
        var notify = config.notifyConfig();
        detail.setAllowWithdraw(behavior == null || !Boolean.FALSE.equals(behavior.getAllowWithdraw()));
        detail.setCommentRequired(behavior != null && Boolean.TRUE.equals(behavior.getCommentRequired()));
        detail.setOnUrge(notify != null && notify.isOnUrge());
    }

    @Override
    public void cancelInstance(String instanceId) {
        workflowProcessService.cancelProcess(instanceId, WorkflowCancelReasons.STARTER_WITHDRAW);
    }

    @Override
    public void terminateInstance(String instanceId, WorkflowInstanceCancelRequest request) {
        String reason = request == null ? null : request.getReason();
        workflowProcessService.terminateProcess(instanceId, reason);
    }

    private void enrichAllowWithdraw(List<? extends WorkflowInstanceVO> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        for (WorkflowInstanceVO row : rows) {
            if (!WfBizProcessStatus.RUNNING.equals(row.getStatus())) {
                row.setAllowWithdraw(false);
                continue;
            }
            if (!StringUtils.hasText(row.getInstanceId())) {
                row.setAllowWithdraw(true);
                continue;
            }
            var behavior = versionConfigLookup
                    .resolveByProcessInstanceId(row.getInstanceId(), null)
                    .behavior();
            row.setAllowWithdraw(behavior == null || !Boolean.FALSE.equals(behavior.getAllowWithdraw()));
        }
    }

    private void enrichBizApproveStatus(List<? extends WorkflowInstanceVO> rows) {
        if (CollectionUtils.isEmpty(rows) || bizHandlers.isEmpty()) {
            return;
        }
        for (WorkflowInstanceVO row : rows) {
            if (!StringUtils.hasText(row.getBizType()) || !StringUtils.hasText(row.getBizId())) {
                continue;
            }
            for (WorkflowBizHandler handler : bizHandlers) {
                if (handler.supports(row.getBizType())) {
                    row.setBizApproveStatus(handler.resolveApproveStatus(row.getBizId()));
                    break;
                }
            }
        }
    }

    private void enrichActiveAssignees(List<? extends WorkflowInstanceVO> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        Set<String> runningIds = rows.stream()
                .filter(vo -> WfBizProcessStatus.RUNNING.equals(vo.getStatus()))
                .map(WorkflowInstanceVO::getInstanceId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, List<Task>> tasksByInstance = new LinkedHashMap<>();
        Set<String> assigneeIds = new LinkedHashSet<>();
        for (String instanceId : runningIds) {
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(instanceId)
                    .active()
                    .list();
            tasksByInstance.put(instanceId, tasks == null ? List.of() : tasks);
            if (tasks != null) {
                for (Task task : tasks) {
                    if (StringUtils.hasText(task.getAssignee())) {
                        assigneeIds.add(task.getAssignee());
                    }
                }
            }
        }
        Map<String, String> names = assigneeIds.isEmpty()
                ? Map.of()
                : userDisplayNameResolver.resolveDisplayNames(assigneeIds);

        for (WorkflowInstanceVO row : rows) {
            if (!WfBizProcessStatus.RUNNING.equals(row.getStatus())) {
                row.setAssigneeUnresolved(false);
                continue;
            }
            List<Task> tasks = tasksByInstance.getOrDefault(row.getInstanceId(), List.of());
            if (tasks.isEmpty()) {
                row.setCurrentTaskName(null);
                row.setCurrentAssigneeUserNames(null);
                row.setAssigneeUnresolved(true);
                continue;
            }
            LinkedHashSet<String> taskNames = new LinkedHashSet<>();
            LinkedHashSet<String> displayNames = new LinkedHashSet<>();
            boolean anyAssignee = false;
            for (Task task : tasks) {
                if (StringUtils.hasText(task.getName())) {
                    taskNames.add(task.getName());
                }
                if (StringUtils.hasText(task.getAssignee())) {
                    anyAssignee = true;
                    String display = names.get(task.getAssignee());
                    displayNames.add(StringUtils.hasText(display) ? display : task.getAssignee());
                }
            }
            row.setCurrentTaskName(taskNames.isEmpty() ? null : LocaleListJoin.join(taskNames));
            row.setCurrentAssigneeUserNames(displayNames.isEmpty() ? null : LocaleListJoin.join(displayNames));
            row.setAssigneeUnresolved(!anyAssignee);
        }
    }

    private WorkflowInstanceVO toInstanceVo(WfBizProcessEntity entity) {
        WorkflowInstanceVO vo = new WorkflowInstanceVO();
        vo.setInstanceId(entity.getProcessInstanceId());
        vo.setProcessDefinitionKey(entity.getProcessDefinitionKey());
        vo.setProcessDefinitionName(workflowModelRegistry.resolveModelName(entity.getProcessDefinitionKey()));
        vo.setBizType(entity.getBizType());
        vo.setBizId(entity.getBizId());
        vo.setBizTitle(entity.getBizTitle());
        vo.setStatus(entity.getStatus());
        vo.setStarterUserId(entity.getStarterUserId());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        if (StringUtils.hasText(entity.getProcessDefinitionKey())) {
            vo.setFormRoute(workflowModelRegistry.resolveFormRoute(entity.getProcessDefinitionKey()));
        }
        return vo;
    }

    private static void copyInstanceFields(WorkflowInstanceDetailVO detail, WorkflowInstanceVO base) {
        detail.setInstanceId(base.getInstanceId());
        detail.setProcessDefinitionKey(base.getProcessDefinitionKey());
        detail.setProcessDefinitionName(base.getProcessDefinitionName());
        detail.setBizType(base.getBizType());
        detail.setBizId(base.getBizId());
        detail.setBizTitle(base.getBizTitle());
        detail.setStatus(base.getStatus());
        detail.setStarterUserId(base.getStarterUserId());
        detail.setStarterUserName(base.getStarterUserName());
        detail.setStartTime(base.getStartTime());
        detail.setEndTime(base.getEndTime());
        detail.setCurrentTaskName(base.getCurrentTaskName());
        detail.setCurrentAssigneeUserNames(base.getCurrentAssigneeUserNames());
        detail.setAssigneeUnresolved(base.getAssigneeUnresolved());
        detail.setFormRoute(base.getFormRoute());
    }

    private void fillStarterDisplayNames(List<? extends WorkflowInstanceVO> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        Set<String> userIds = rows.stream()
                .map(WorkflowInstanceVO::getStarterUserId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        Map<String, String> names = userDisplayNameResolver.resolveDisplayNames(userIds);
        for (WorkflowInstanceVO row : rows) {
            if (!StringUtils.hasText(row.getStarterUserId())) {
                continue;
            }
            String display = names.get(row.getStarterUserId());
            if (StringUtils.hasText(display)) {
                row.setStarterUserName(display);
            }
        }
    }

    private static WorkflowInstancePageQuery copyQuery(WorkflowInstancePageQuery query) {
        WorkflowInstancePageQuery copy = new WorkflowInstancePageQuery();
        if (query == null) {
            return copy;
        }
        copy.setPageNo(query.getPageNo());
        copy.setPageSize(query.getPageSize());
        copy.setBizTitle(query.getBizTitle());
        copy.setBizType(query.getBizType());
        copy.setStatus(query.getStatus());
        copy.setUnresolvedOnly(query.getUnresolvedOnly());
        return copy;
    }

    private static String requireUserId() {
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("error.unauthorized");
        }
        return userId;
    }

    private static String requireTenantId() {
        String tenantId = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException("error.unauthorized");
        }
        return tenantId;
    }
}
