package com.icoolkj.platform.workflow.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskAddSignRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskCompleteRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskDelegateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskJumpRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskPageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowTaskRemoveSignRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskTransferRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskUrgeRequest;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import com.icoolkj.platform.workflow.service.WorkflowProcessService;
import com.icoolkj.platform.workflow.service.WorkflowTaskService;
import com.icoolkj.platform.workflow.spi.WorkflowUserDisplayNameResolver;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import com.icoolkj.platform.workflow.domain.WfTaskActionEntity;
import com.icoolkj.platform.workflow.mapper.WfTaskActionMapper;
import com.icoolkj.platform.workflow.support.WorkflowAddSignAuditSupport;
import com.icoolkj.platform.workflow.support.WorkflowBizProcessLookup;
import com.icoolkj.platform.workflow.support.WorkflowCcReceiptSupport;
import com.icoolkj.platform.workflow.support.WorkflowCcTaskSupport;
import com.icoolkj.platform.workflow.support.WorkflowDesignNodeLookup;
import com.icoolkj.platform.workflow.support.WorkflowMappingSupport;
import com.icoolkj.platform.workflow.support.WorkflowPassModes;
import com.icoolkj.platform.workflow.vo.WorkflowCommentVO;
import com.icoolkj.platform.workflow.vo.WorkflowParallelAssigneeVO;
import com.icoolkj.platform.workflow.vo.WorkflowTaskDetailVO;
import com.icoolkj.platform.workflow.vo.WorkflowTaskVO;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 工作流任务服务 */
@Service
public class WorkflowTaskServiceImpl implements WorkflowTaskService {

    private final TaskService taskService;
    private final HistoryService historyService;
    private final WorkflowProcessService workflowProcessService;
    private final WorkflowModelRegistry workflowModelRegistry;
    private final WorkflowUserDisplayNameResolver userDisplayNameResolver;
    private final WorkflowDesignNodeLookup designNodeLookup;
    private final WfTaskActionMapper wfTaskActionMapper;
    private final WorkflowCcReceiptSupport ccReceiptSupport;
    private final WorkflowBizProcessLookup workflowBizProcessLookup;

    public WorkflowTaskServiceImpl(TaskService taskService,
                                   HistoryService historyService,
                                   WorkflowProcessService workflowProcessService,
                                   WorkflowModelRegistry workflowModelRegistry,
                                   WorkflowUserDisplayNameResolver userDisplayNameResolver,
                                   WorkflowDesignNodeLookup designNodeLookup,
                                   WfTaskActionMapper wfTaskActionMapper,
                                   WorkflowCcReceiptSupport ccReceiptSupport,
                                   WorkflowBizProcessLookup workflowBizProcessLookup) {
        this.taskService = taskService;
        this.historyService = historyService;
        this.workflowProcessService = workflowProcessService;
        this.workflowModelRegistry = workflowModelRegistry;
        this.userDisplayNameResolver = userDisplayNameResolver;
        this.designNodeLookup = designNodeLookup;
        this.wfTaskActionMapper = wfTaskActionMapper;
        this.ccReceiptSupport = ccReceiptSupport;
        this.workflowBizProcessLookup = workflowBizProcessLookup;
    }

    @Override
    public PageResult<WorkflowTaskVO> pageTodo(WorkflowTaskPageQuery query) {
        String userId = requireUserId();
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userId)
                .active()
                .orderByTaskCreateTime()
                .desc()
                .list();
        List<WorkflowTaskVO> rows = tasks.stream()
                .filter(task -> !WorkflowCcTaskSupport.isCcTask(task))
                .map(task -> enrichTaskVo(WorkflowMappingSupport.toTaskVo(task,
                        workflowProcessService.getProcessVariables(task.getProcessInstanceId()))))
                .filter(vo -> WorkflowMappingSupport.matchesTaskQuery(
                        vo, query.getBizTitle(), query.getBizType(),
                        query.getProcessDefinitionKey(), query.getTaskName()))
                .collect(Collectors.toList());
        fillStarterDisplayNames(rows);
        return WorkflowMappingSupport.paginate(rows, query);
    }

    @Override
    public PageResult<WorkflowTaskVO> pageDone(WorkflowTaskPageQuery query) {
        String userId = requireUserId();
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
        List<WorkflowTaskVO> rows = tasks.stream()
                .filter(task -> !WorkflowCcTaskSupport.isCcTask(task))
                .filter(task -> WorkflowMappingSupport.isUserHandledHistoricTask(task, null))
                .map(task -> enrichTaskVo(WorkflowMappingSupport.toHistoricTaskVo(task,
                        workflowProcessService.getProcessVariables(task.getProcessInstanceId()))))
                .collect(Collectors.toCollection(ArrayList::new));
        Set<String> coveredTaskIds = rows.stream()
                .map(WorkflowTaskVO::getTaskId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        // 旧数据：仅 setAssignee 交接时无 wf_task_action 补已办；新交接已落 HI 则跳过
        rows.addAll(listAddSignHandoffDoneRows(userId, coveredTaskIds));
        rows.sort(Comparator.comparing(WorkflowTaskVO::getEndTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        rows = rows.stream()
                .filter(vo -> WorkflowMappingSupport.matchesTaskQuery(
                        vo, query.getBizTitle(), query.getBizType(),
                        query.getProcessDefinitionKey(), query.getTaskName()))
                .collect(Collectors.toList());
        fillStarterDisplayNames(rows);
        return WorkflowMappingSupport.paginate(rows, query);
    }

    @Override
    public PageResult<WorkflowTaskVO> pageCc(WorkflowTaskPageQuery query) {
        String userId = requireUserId();
        List<WorkflowTaskVO> active = taskService.createTaskQuery()
                .taskAssignee(userId)
                .active()
                .orderByTaskCreateTime()
                .desc()
                .list()
                .stream()
                .filter(WorkflowCcTaskSupport::isCcTask)
                .map(task -> {
                    ensureCcReceipt(task, userId);
                    return enrichTaskVo(WorkflowMappingSupport.toTaskVo(task,
                            workflowProcessService.getProcessVariables(task.getProcessInstanceId())));
                })
                .filter(vo -> WorkflowMappingSupport.matchesTaskQuery(
                        vo, query.getBizTitle(), query.getBizType(),
                        query.getProcessDefinitionKey(), query.getTaskName()))
                .collect(Collectors.toList());
        fillCcReadStatus(active, userId);

        List<HistoricTaskInstance> historic = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
        List<WorkflowTaskVO> finished = historic.stream()
                .filter(WorkflowCcTaskSupport::isCcTask)
                .filter(task -> WorkflowMappingSupport.isUserHandledHistoricTask(task, null))
                .map(task -> enrichTaskVo(WorkflowMappingSupport.toHistoricTaskVo(task,
                        workflowProcessService.getProcessVariables(task.getProcessInstanceId()))))
                .filter(vo -> WorkflowMappingSupport.matchesTaskQuery(
                        vo, query.getBizTitle(), query.getBizType(),
                        query.getProcessDefinitionKey(), query.getTaskName()))
                .collect(Collectors.toList());
        fillCcReadStatus(finished, userId);

        List<WorkflowTaskVO> rows = new java.util.ArrayList<>(active.size() + finished.size());
        rows.addAll(active);
        rows.addAll(finished);
        fillStarterDisplayNames(rows);
        return WorkflowMappingSupport.paginate(rows, query);
    }

    @Override
    public WorkflowTaskDetailVO getByTaskId(String taskId) {
        String userId = requireUserId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        HistoricTaskInstance historicTask = null;
        if (task == null) {
            historicTask = historyService.createHistoricTaskInstanceQuery()
                    .taskId(taskId)
                    .singleResult();
            if (historicTask == null) {
                throw new BusinessException("error.workflow.task_not_found");
            }
            if (!userId.equals(historicTask.getAssignee())) {
                throw new BusinessException("error.workflow.task_not_assignee");
            }
        } else if (!userId.equals(task.getAssignee())) {
            throw new BusinessException("error.workflow.task_not_assignee");
        }

        String processInstanceId = task != null ? task.getProcessInstanceId() : historicTask.getProcessInstanceId();
        Map<String, Object> variables = WorkflowMappingSupport.toVariableMap(
                workflowProcessService.getProcessVariables(processInstanceId));

        WorkflowTaskDetailVO detail = new WorkflowTaskDetailVO();
        if (task != null) {
            WorkflowTaskVO base = enrichTaskVo(WorkflowMappingSupport.toTaskVo(task, variables));
            copyTaskFields(detail, base);
        } else {
            WorkflowTaskVO base = enrichTaskVo(WorkflowMappingSupport.toHistoricTaskVo(historicTask, variables));
            copyTaskFields(detail, base);
        }
        detail.setVariables(variables);
        fillStarterDisplayNames(List.of(detail));
        detail.setCommentList(loadComments(processInstanceId));
        String taskKey = task != null ? task.getTaskDefinitionKey()
                : (historicTask == null ? null : historicTask.getTaskDefinitionKey());
        if (Boolean.TRUE.equals(detail.getCcTask()) && task != null) {
            ensureCcReceipt(task, userId);
            detail.setCcRead(ccReceiptSupport.isRead(task.getId(), userId));
        } else if (Boolean.TRUE.equals(detail.getCcTask()) && historicTask != null) {
            detail.setCcRead(ccReceiptSupport.isRead(historicTask.getId(), userId));
        }
        if (StringUtils.hasText(taskKey)) {
            WorkflowModelDesignNodeRequest node = designNodeLookup.findByProcessInstanceAndTaskKey(
                    processInstanceId, taskKey);
            detail.setRejectMode(WorkflowDesignNodeLookup.rejectModeOf(node));
            detail.setAllowTransfer(WorkflowDesignNodeLookup.allowTransfer(node));
            detail.setAllowDelegate(WorkflowDesignNodeLookup.allowDelegate(node));
            boolean allowAddSign = WorkflowDesignNodeLookup.allowAddSign(node);
            detail.setAllowAddSign(allowAddSign);
            boolean multi = node != null
                    && WorkflowPassModes.isMultiInstance(node.getPassMode());
            detail.setAllowRemoveSign(allowAddSign && multi && task != null);
            if (Boolean.TRUE.equals(detail.getAllowRemoveSign())) {
                detail.setParallelAssignees(loadRemovableParallelAssignees(task));
            }
        }
        return detail;
    }

    private List<WorkflowParallelAssigneeVO> loadRemovableParallelAssignees(Task current) {
        if (current == null) {
            return List.of();
        }
        List<Task> siblings = taskService.createTaskQuery()
                .processInstanceId(current.getProcessInstanceId())
                .taskDefinitionKey(current.getTaskDefinitionKey())
                .active()
                .list();
        List<WorkflowParallelAssigneeVO> rows = new ArrayList<>();
        Set<String> userIds = new HashSet<>();
        for (Task sibling : siblings) {
            if (sibling.getId().equals(current.getId()) || !StringUtils.hasText(sibling.getAssignee())) {
                continue;
            }
            WorkflowParallelAssigneeVO row = new WorkflowParallelAssigneeVO();
            row.setTaskId(sibling.getId());
            row.setUserId(sibling.getAssignee());
            rows.add(row);
            userIds.add(sibling.getAssignee());
        }
        if (!userIds.isEmpty()) {
            Map<String, String> names = userDisplayNameResolver.resolveDisplayNames(userIds);
            for (WorkflowParallelAssigneeVO row : rows) {
                String name = names.get(row.getUserId());
                row.setUserName(StringUtils.hasText(name) ? name : row.getUserId());
            }
        }
        rows.sort(Comparator.comparing(WorkflowParallelAssigneeVO::getUserName,
                Comparator.nullsLast(String::compareToIgnoreCase)));
        return rows;
    }

    @Override
    public void completeTask(String taskId, WorkflowTaskCompleteRequest request) {
        workflowProcessService.completeTask(taskId, request);
    }

    @Override
    public void transferTask(String taskId, WorkflowTaskTransferRequest request) {
        workflowProcessService.transferTask(taskId, request);
    }

    @Override
    public void delegateTask(String taskId, WorkflowTaskDelegateRequest request) {
        workflowProcessService.delegateTask(taskId, request);
    }

    @Override
    public void jumpTask(String taskId, WorkflowTaskJumpRequest request) {
        workflowProcessService.jumpTask(taskId, request);
    }

    @Override
    public void urgeTask(String taskId, WorkflowTaskUrgeRequest request) {
        workflowProcessService.urgeTask(taskId, request);
    }

    @Override
    public void addSignTask(String taskId, WorkflowTaskAddSignRequest request) {
        workflowProcessService.addSignTask(taskId, request);
    }

    @Override
    public void removeSignTask(String taskId, WorkflowTaskRemoveSignRequest request) {
        workflowProcessService.removeSignTask(taskId, request);
    }

    @Override
    public void markCcRead(String taskId) {
        String userId = requireUserId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        HistoricTaskInstance historic = null;
        if (task == null) {
            historic = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            if (historic == null) {
                throw new BusinessException("error.workflow.task_not_found");
            }
            if (!userId.equals(historic.getAssignee())) {
                throw new BusinessException("error.workflow.task_not_assignee");
            }
            if (!WorkflowCcTaskSupport.isCcTask(historic)) {
                throw new BusinessException("error.workflow.cc_action_forbidden");
            }
            ccReceiptSupport.markRead(taskId, userId);
            return;
        }
        if (!userId.equals(task.getAssignee())) {
            throw new BusinessException("error.workflow.task_not_assignee");
        }
        if (!WorkflowCcTaskSupport.isCcTask(task)) {
            throw new BusinessException("error.workflow.cc_action_forbidden");
        }
        ensureCcReceipt(task, userId);
        ccReceiptSupport.markRead(taskId, userId);
    }

    private void ensureCcReceipt(Task task, String userId) {
        if (task == null || !StringUtils.hasText(userId)) {
            return;
        }
        WfBizProcessEntity biz = workflowBizProcessLookup.findByProcessInstanceId(task.getProcessInstanceId());
        String tenantId = biz == null ? TenantConstants.PLATFORM_TENANT_ID : biz.getTenantId();
        ccReceiptSupport.ensureReceipt(tenantId, task.getProcessInstanceId(), task.getId(), userId);
    }

    private void fillCcReadStatus(List<WorkflowTaskVO> rows, String userId) {
        if (CollectionUtils.isEmpty(rows) || !StringUtils.hasText(userId)) {
            return;
        }
        List<String> taskIds = rows.stream()
                .map(WorkflowTaskVO::getTaskId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        Map<String, Boolean> readMap = ccReceiptSupport.readStatusByTaskIds(taskIds, userId);
        for (WorkflowTaskVO row : rows) {
            if (!Boolean.TRUE.equals(row.getCcTask()) || !StringUtils.hasText(row.getTaskId())) {
                continue;
            }
            row.setCcRead(Boolean.TRUE.equals(readMap.get(row.getTaskId())));
        }
    }

    private List<WorkflowCommentVO> loadComments(String processInstanceId) {
        List<Comment> comments = taskService.getProcessInstanceComments(processInstanceId);
        List<WorkflowCommentVO> rows = comments.stream()
                .sorted(Comparator.comparing(Comment::getTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(comment -> {
                    WorkflowCommentVO vo = new WorkflowCommentVO();
                    vo.setUserId(comment.getUserId());
                    vo.setMessage(comment.getFullMessage());
                    vo.setTime(WorkflowMappingSupport.toLocalDateTime(comment.getTime()));
                    return vo;
                })
                .collect(Collectors.toList());
        Set<String> userIds = rows.stream()
                .map(WorkflowCommentVO::getUserId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, String> names = userDisplayNameResolver.resolveDisplayNames(userIds);
        for (WorkflowCommentVO row : rows) {
            if (StringUtils.hasText(row.getUserId()) && names.containsKey(row.getUserId())) {
                row.setUserName(names.get(row.getUserId()));
            }
        }
        return rows;
    }

    private void fillStarterDisplayNames(List<? extends WorkflowTaskVO> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        Set<String> userIds = rows.stream()
                .map(WorkflowTaskVO::getStarterUserId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        Map<String, String> names = userDisplayNameResolver.resolveDisplayNames(userIds);
        for (WorkflowTaskVO row : rows) {
            if (!StringUtils.hasText(row.getStarterUserId())) {
                continue;
            }
            String display = names.get(row.getStarterUserId());
            if (StringUtils.hasText(display)) {
                row.setStarterUserName(display);
            }
        }
    }

    private static void copyTaskFields(WorkflowTaskDetailVO detail, WorkflowTaskVO base) {
        detail.setTaskId(base.getTaskId());
        detail.setTaskName(base.getTaskName());
        detail.setProcessInstanceId(base.getProcessInstanceId());
        detail.setProcessDefinitionKey(base.getProcessDefinitionKey());
        detail.setProcessDefinitionName(base.getProcessDefinitionName());
        detail.setBizType(base.getBizType());
        detail.setBizId(base.getBizId());
        detail.setBizTitle(base.getBizTitle());
        detail.setStarterUserId(base.getStarterUserId());
        detail.setStarterUserName(base.getStarterUserName());
        detail.setFormRoute(base.getFormRoute());
        detail.setCreateTime(base.getCreateTime());
        detail.setDueDate(base.getDueDate());
        detail.setEndTime(base.getEndTime());
        detail.setApproveResult(base.getApproveResult());
        detail.setCcTask(base.getCcTask());
        detail.setCcRead(base.getCcRead());
    }

    private WorkflowTaskVO enrichTaskVo(WorkflowTaskVO vo) {
        if (vo == null) {
            return null;
        }
        if (StringUtils.hasText(vo.getProcessDefinitionKey())) {
            vo.setProcessDefinitionName(workflowModelRegistry.resolveModelName(vo.getProcessDefinitionKey()));
            if (!StringUtils.hasText(vo.getFormRoute())) {
                vo.setFormRoute(workflowModelRegistry.resolveFormRoute(vo.getProcessDefinitionKey()));
            }
        }
        return vo;
    }

    private List<WorkflowTaskVO> listAddSignHandoffDoneRows(String userId, Set<String> coveredTaskIds) {
        List<WfTaskActionEntity> actions = TenantScopeRunner.runIgnoringTenant(
                () -> wfTaskActionMapper.selectAddSignHandoffsByFromUserId(userId));
        if (CollectionUtils.isEmpty(actions)) {
            return List.of();
        }
        List<WorkflowTaskVO> rows = new ArrayList<>();
        for (WfTaskActionEntity action : actions) {
            if (!WorkflowAddSignAuditSupport.isApprovalHandoffAction(action)) {
                continue;
            }
            if (coveredTaskIds != null && StringUtils.hasText(action.getTaskId())
                    && coveredTaskIds.contains(action.getTaskId())) {
                continue;
            }
            String pi = action.getProcessInstanceId();
            Map<String, Object> variables = workflowProcessService.getProcessVariables(pi);
            String processDefinitionKey = resolveProcessDefinitionKey(pi);
            String taskName = resolveTaskName(action.getTaskId(), action.getTaskDefinitionKey(), pi);
            rows.add(enrichTaskVo(WorkflowAddSignAuditSupport.toDoneTaskVo(
                    action, taskName, processDefinitionKey, variables)));
        }
        return rows;
    }

    private String resolveProcessDefinitionKey(String processInstanceId) {
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (instance == null) {
            return null;
        }
        return WorkflowMappingSupport.extractProcessKey(instance.getProcessDefinitionId());
    }

    private String resolveTaskName(String taskId, String taskDefinitionKey, String processInstanceId) {
        if (StringUtils.hasText(taskId)) {
            HistoricTaskInstance historic = historyService.createHistoricTaskInstanceQuery()
                    .taskId(taskId)
                    .singleResult();
            if (historic != null && StringUtils.hasText(historic.getName())) {
                return historic.getName();
            }
            Task runtime = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (runtime != null && StringUtils.hasText(runtime.getName())) {
                return runtime.getName();
            }
        }
        if (StringUtils.hasText(taskDefinitionKey) && StringUtils.hasText(processInstanceId)) {
            HistoricTaskInstance byKey = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .taskDefinitionKey(taskDefinitionKey)
                    .orderByHistoricTaskInstanceStartTime()
                    .desc()
                    .list()
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (byKey != null && StringUtils.hasText(byKey.getName())) {
                return byKey.getName();
            }
        }
        return taskDefinitionKey;
    }

    private static String requireUserId() {
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("error.unauthorized");
        }
        return userId;
    }
}
