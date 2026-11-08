package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.workflow.constant.WfBizProcessStatus;
import com.icoolkj.platform.workflow.constant.WfTaskActionTypes;
import com.icoolkj.platform.workflow.constant.WorkflowCancelReasons;
import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import com.icoolkj.platform.workflow.domain.WfModelVersionEntity;
import com.icoolkj.platform.workflow.domain.WfTaskActionEntity;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignBranchRequest;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.mapper.WfBizProcessMapper;
import com.icoolkj.platform.workflow.mapper.WfModelVersionMapper;
import com.icoolkj.platform.workflow.mapper.WfTaskActionMapper;
import com.icoolkj.platform.workflow.spi.WorkflowUserDisplayNameResolver;
import com.icoolkj.platform.workflow.vo.WorkflowHistoricTaskVO;
import com.icoolkj.platform.workflow.vo.WorkflowInstanceDetailVO;
import com.icoolkj.platform.workflow.vo.WorkflowProcessCycleVO;
import com.icoolkj.platform.workflow.vo.WorkflowProgressNodeVO;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 构建流程实例时间线与 BPMN 节点进度（同单据多轮提交串联） */
@Component
public class WorkflowInstanceTimelineBuilder {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_REJECTED = "rejected";
    /** 条件/并行未走到的节点（流程已结束或兄弟分支已到达） */
    private static final String STATUS_SKIPPED = "skipped";

    private final HistoryService historyService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final WfBizProcessMapper wfBizProcessMapper;
    private final WfModelVersionMapper wfModelVersionMapper;
    private final WfTaskActionMapper wfTaskActionMapper;
    private final WorkflowUserDisplayNameResolver userDisplayNameResolver;
    private final WorkflowDesignSoTSupport designSoTSupport;

    public WorkflowInstanceTimelineBuilder(HistoryService historyService,
                                           TaskService taskService,
                                           RepositoryService repositoryService,
                                           WfBizProcessMapper wfBizProcessMapper,
                                           WfModelVersionMapper wfModelVersionMapper,
                                           WfTaskActionMapper wfTaskActionMapper,
                                           WorkflowUserDisplayNameResolver userDisplayNameResolver,
                                           WorkflowDesignSoTSupport designSoTSupport) {
        this.historyService = historyService;
        this.taskService = taskService;
        this.repositoryService = repositoryService;
        this.wfBizProcessMapper = wfBizProcessMapper;
        this.wfModelVersionMapper = wfModelVersionMapper;
        this.wfTaskActionMapper = wfTaskActionMapper;
        this.userDisplayNameResolver = userDisplayNameResolver;
        this.designSoTSupport = designSoTSupport;
    }

    public void enrich(WorkflowInstanceDetailVO detail, String instanceId, Map<String, Object> variables) {
        if (detail == null || !StringUtils.hasText(instanceId)) {
            return;
        }

        List<HistoricTaskInstance> currentHistoric = listHistoricTasks(instanceId);
        List<Task> currentActive = listActiveTasks(instanceId);
        Map<String, TaskComment> currentComments = loadCommentsByTaskId(instanceId);

        Set<String> userIds = collectUserIds(currentHistoric, currentActive, variables, currentComments);
        List<WfBizProcessEntity> cycles = resolveCycles(instanceId, detail.getBizType(), detail.getBizId());
        for (WfBizProcessEntity cycle : cycles) {
            if (StringUtils.hasText(cycle.getStarterUserId())) {
                userIds.add(cycle.getStarterUserId());
            }
            String cyclePi = cycle.getProcessInstanceId();
            if (!instanceId.equals(cyclePi)) {
                List<HistoricTaskInstance> historic = listHistoricTasks(cyclePi);
                Map<String, TaskComment> cycleComments = loadCommentsByTaskId(cyclePi);
                userIds.addAll(collectUserIds(historic, List.of(), Map.of(), cycleComments));
            }
            for (WfTaskActionEntity action : listTimelineActions(cyclePi)) {
                if (StringUtils.hasText(action.getFromUserId())) {
                    userIds.add(action.getFromUserId());
                }
                if (StringUtils.hasText(action.getToUserId())) {
                    userIds.add(action.getToUserId());
                }
            }
        }
        Map<String, String> displayNames = userDisplayNameResolver.resolveDisplayNames(userIds);

        if (StringUtils.hasText(detail.getStarterUserId())) {
            detail.setStarterUserName(resolveName(displayNames, detail.getStarterUserId(),
                    asString(variables.get(WorkflowVariables.STARTER_USER_NAME))));
        }

        List<WorkflowProcessCycleVO> cycleVos = new ArrayList<>();
        List<WorkflowHistoricTaskVO> mergedTasks = new ArrayList<>();
        int cycleNo = 1;
        for (WfBizProcessEntity cycle : cycles) {
            String cycleInstanceId = cycle.getProcessInstanceId();
            boolean current = instanceId.equals(cycleInstanceId);
            Map<String, Object> cycleVars = current
                    ? variables
                    : loadProcessVariables(cycleInstanceId);
            List<HistoricTaskInstance> historic = current
                    ? currentHistoric
                    : listHistoricTasks(cycleInstanceId);
            List<Task> active = current ? currentActive : List.of();
            Map<String, TaskComment> comments = current
                    ? currentComments
                    : loadCommentsByTaskId(cycleInstanceId);

            WorkflowProcessCycleVO cycleVo = new WorkflowProcessCycleVO();
            cycleVo.setCycleNo(cycleNo);
            cycleVo.setProcessInstanceId(cycleInstanceId);
            cycleVo.setStatus(cycle.getStatus());
            cycleVo.setStarterUserId(cycle.getStarterUserId());
            cycleVo.setStarterUserName(resolveName(displayNames, cycle.getStarterUserId(), null));
            cycleVo.setStartTime(cycle.getStartTime());
            cycleVo.setEndTime(cycle.getEndTime());
            cycleVos.add(cycleVo);

            List<WorkflowHistoricTaskVO> timeline = buildTimeline(
                    historic, active, cycleVars, comments, displayNames, cycleInstanceId);
            appendLegacyWithdrawIfNeeded(timeline, cycle, displayNames);
            for (WorkflowHistoricTaskVO task : timeline) {
                task.setCycleNo(cycleNo);
                task.setProcessInstanceId(cycleInstanceId);
            }
            mergedTasks.addAll(timeline);
            cycleNo++;
        }

        detail.setProcessCycles(cycleVos);
        detail.setTasks(mergedTasks);
        detail.setCurrentTaskId(currentActive.isEmpty() ? null : currentActive.get(0).getId());

        HistoricProcessInstance historicProcess = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId)
                .singleResult();
        if (historicProcess != null) {
            String designJson = resolveDesignJson(historicProcess.getProcessDefinitionId());
            detail.setDesignJson(designJson);
            detail.setProgressNodes(buildProgressNodes(designJson, historicProcess.getProcessDefinitionId(),
                    currentHistoric, currentActive, variables, currentComments, displayNames,
                    historicProcess.getEndTime() != null));
        }
    }

    /** 审批流程图 SoT：实例对应版本的 design_json（无则 null，前端退化为 progressNodes） */
    private String resolveDesignJson(String processDefinitionId) {
        if (!StringUtils.hasText(processDefinitionId)) {
            return null;
        }
        WfModelVersionEntity version = TenantScopeRunner.runIgnoringTenant(
                () -> wfModelVersionMapper.selectByProcessDefinitionId(processDefinitionId));
        return version == null ? null : version.getDesignJson();
    }

    private List<WfBizProcessEntity> resolveCycles(String instanceId, String bizType, String bizId) {
        WfBizProcessEntity current = TenantScopeRunner.runIgnoringTenant(
                () -> wfBizProcessMapper.selectByProcessInstanceId(instanceId));
        String type = StringUtils.hasText(bizType) ? bizType : (current == null ? null : current.getBizType());
        String id = StringUtils.hasText(bizId) ? bizId : (current == null ? null : current.getBizId());
        if (!StringUtils.hasText(type) || !StringUtils.hasText(id)) {
            return current == null ? List.of() : List.of(current);
        }
        List<WfBizProcessEntity> all = TenantScopeRunner.runIgnoringTenant(
                () -> wfBizProcessMapper.selectListByBiz(type, id));
        if (CollectionUtils.isEmpty(all)) {
            return current == null ? List.of() : List.of(current);
        }
        return all;
    }

    private Map<String, Object> loadProcessVariables(String processInstanceId) {
        HistoricProcessInstance historic = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeProcessVariables()
                .singleResult();
        if (historic != null && historic.getProcessVariables() != null) {
            return historic.getProcessVariables();
        }
        return Map.of();
    }

    private List<HistoricTaskInstance> listHistoricTasks(String instanceId) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId)
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceStartTime()
                .asc()
                .list();
    }

    private List<Task> listActiveTasks(String instanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(instanceId)
                .active()
                .orderByTaskCreateTime()
                .asc()
                .list();
    }

    private List<WorkflowHistoricTaskVO> buildTimeline(List<HistoricTaskInstance> historicTasks,
                                                       List<Task> activeTasks,
                                                       Map<String, Object> variables,
                                                       Map<String, TaskComment> commentsByTaskId,
                                                       Map<String, String> displayNames,
                                                       String processInstanceId) {
        Map<String, Task> activeByDefKey = activeTasks.stream()
                .filter(task -> StringUtils.hasText(task.getTaskDefinitionKey()))
                .collect(Collectors.toMap(Task::getTaskDefinitionKey, task -> task, (a, b) -> a, LinkedHashMap::new));

        List<WorkflowHistoricTaskVO> rows = new ArrayList<>();
        Set<String> emittedActiveKeys = new HashSet<>();

        for (HistoricTaskInstance historic : historicTasks) {
            if (!WorkflowMappingSupport.isUserHandledHistoricTask(historic, Set.of())) {
                continue;
            }
            WorkflowHistoricTaskVO vo = toHistoricVo(historic, variables, commentsByTaskId, displayNames);
            vo.setNodeStatus(STATUS_COMPLETED);
            Task active = activeByDefKey.get(historic.getTaskDefinitionKey());
            if (active != null) {
                vo.setNodeStatus(STATUS_ACTIVE);
                emittedActiveKeys.add(historic.getTaskDefinitionKey());
            }
            rows.add(vo);
        }

        for (Task active : activeTasks) {
            if (emittedActiveKeys.contains(active.getTaskDefinitionKey())) {
                continue;
            }
            boolean alreadyInHistoric = historicTasks.stream()
                    .anyMatch(h -> active.getId().equals(h.getId()));
            if (alreadyInHistoric) {
                continue;
            }
            WorkflowHistoricTaskVO vo = new WorkflowHistoricTaskVO();
            vo.setTaskId(active.getId());
            vo.setTaskName(active.getName());
            vo.setTaskDefinitionKey(active.getTaskDefinitionKey());
            vo.setNodeStatus(STATUS_ACTIVE);
            String assignee = resolveAssigneeUserId(active.getAssignee(), active.getId(), commentsByTaskId);
            vo.setAssigneeUserId(assignee);
            vo.setAssigneeUserName(resolveName(displayNames, assignee, null));
            vo.setStartTime(WorkflowMappingSupport.toLocalDateTime(active.getCreateTime()));
            TaskComment comment = commentsByTaskId.get(active.getId());
            vo.setComment(comment == null ? null : comment.message());
            rows.add(vo);
        }

        Set<String> handledHistoricTaskIds = historicTasks.stream()
                .filter(h -> WorkflowMappingSupport.isUserHandledHistoricTask(h, Set.of()))
                .map(HistoricTaskInstance::getId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, String> taskNameByKey = resolveTaskNamesByDefinitionKey(historicTasks, activeTasks);
        for (WfTaskActionEntity action : listTimelineActions(processInstanceId)) {
            // 加签交接已落 HI 时跳过合成行；转办/委派/撤回等始终展示
            if (WorkflowTimelineActionSupport.skipWhenHistoricExists(action)
                    && StringUtils.hasText(action.getTaskId())
                    && handledHistoricTaskIds.contains(action.getTaskId())) {
                continue;
            }
            String taskName = taskNameByKey.get(action.getTaskDefinitionKey());
            rows.add(WorkflowTimelineActionSupport.toTimelineTask(action, taskName, displayNames));
        }

        rows.sort(Comparator
                .comparing(WorkflowHistoricTaskVO::getEndTime,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(WorkflowHistoricTaskVO::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(WorkflowHistoricTaskVO::getTaskId,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        return rows;
    }

    /** 历史撤回未写 wf_task_action 时，用 cancelled 周期补一条展示行 */
    private void appendLegacyWithdrawIfNeeded(List<WorkflowHistoricTaskVO> timeline,
                                              WfBizProcessEntity cycle,
                                              Map<String, String> displayNames) {
        if (cycle == null || !WfBizProcessStatus.CANCELLED.equals(cycle.getStatus())) {
            return;
        }
        boolean hasWithdraw = timeline.stream()
                .anyMatch(t -> WfTaskActionTypes.WITHDRAW.equals(t.getActionType()));
        if (hasWithdraw) {
            return;
        }
        WorkflowHistoricTaskVO vo = new WorkflowHistoricTaskVO();
        vo.setTaskId("withdraw:" + cycle.getProcessInstanceId());
        vo.setTaskName("withdraw");
        vo.setActionType(WfTaskActionTypes.WITHDRAW);
        vo.setNodeStatus("cancelled");
        vo.setAssigneeUserId(cycle.getStarterUserId());
        vo.setAssigneeUserName(resolveName(displayNames, cycle.getStarterUserId(), null));
        vo.setApproved(Boolean.FALSE);
        vo.setReasonCode(WorkflowCancelReasons.DEFAULT);
        vo.setStartTime(cycle.getEndTime() != null ? cycle.getEndTime() : cycle.getStartTime());
        vo.setEndTime(cycle.getEndTime() != null ? cycle.getEndTime() : cycle.getStartTime());
        vo.setProcessInstanceId(cycle.getProcessInstanceId());
        timeline.add(vo);
    }

    private List<WfTaskActionEntity> listTimelineActions(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return List.of();
        }
        List<WfTaskActionEntity> actions = TenantScopeRunner.runIgnoringTenant(
                () -> wfTaskActionMapper.selectByProcessInstanceId(processInstanceId));
        if (CollectionUtils.isEmpty(actions)) {
            return List.of();
        }
        return actions.stream()
                .filter(WorkflowTimelineActionSupport::isTimelineAction)
                .collect(Collectors.toList());
    }

    private static Map<String, String> resolveTaskNamesByDefinitionKey(List<HistoricTaskInstance> historicTasks,
                                                                       List<Task> activeTasks) {
        Map<String, String> names = new LinkedHashMap<>();
        for (HistoricTaskInstance historic : historicTasks) {
            if (StringUtils.hasText(historic.getTaskDefinitionKey())
                    && StringUtils.hasText(historic.getName())) {
                names.putIfAbsent(historic.getTaskDefinitionKey(), historic.getName());
            }
        }
        for (Task active : activeTasks) {
            if (StringUtils.hasText(active.getTaskDefinitionKey())
                    && StringUtils.hasText(active.getName())) {
                names.putIfAbsent(active.getTaskDefinitionKey(), active.getName());
            }
        }
        return names;
    }

    private List<WorkflowProgressNodeVO> buildProgressNodes(String designJson,
                                                            String processDefinitionId,
                                                            List<HistoricTaskInstance> historicTasks,
                                                            List<Task> activeTasks,
                                                            Map<String, Object> variables,
                                                            Map<String, TaskComment> commentsByTaskId,
                                                            Map<String, String> displayNames,
                                                            boolean processEnded) {
        List<WorkflowDesignSoTSupport.DesignLeafTask> leafTasks =
                new ArrayList<>(designSoTSupport.collectApproverLeafTasks(designJson));
        if (leafTasks.isEmpty()) {
            // 无 design 时退回 BPMN UserTask（逃生舱 / 旧数据）
            for (UserTask userTask : loadOrderedUserTasks(processDefinitionId)) {
                leafTasks.add(new WorkflowDesignSoTSupport.DesignLeafTask(
                        userTask.getId(),
                        StringUtils.hasText(userTask.getName()) ? userTask.getName() : userTask.getId()));
            }
        }
        if (leafTasks.isEmpty()) {
            return List.of();
        }

        Map<String, HistoricTaskInstance> latestHistoricByKey = new LinkedHashMap<>();
        for (HistoricTaskInstance task : historicTasks) {
            if (!StringUtils.hasText(task.getTaskDefinitionKey())) {
                continue;
            }
            if (!WorkflowMappingSupport.isUserHandledHistoricTask(task, Set.of())) {
                continue;
            }
            latestHistoricByKey.put(task.getTaskDefinitionKey(), task);
        }
        Map<String, Task> activeByKey = activeTasks.stream()
                .filter(t -> StringUtils.hasText(t.getTaskDefinitionKey()))
                .collect(Collectors.toMap(Task::getTaskDefinitionKey, t -> t, (a, b) -> a));

        List<WorkflowProgressNodeVO> nodes = new ArrayList<>();
        boolean blockedAfterReject = false;

        for (WorkflowDesignSoTSupport.DesignLeafTask leaf : leafTasks) {
            WorkflowProgressNodeVO node = new WorkflowProgressNodeVO();
            node.setNodeKey(leaf.taskDefinitionKey());
            node.setNodeName(leaf.name());

            if (blockedAfterReject) {
                node.setNodeStatus(STATUS_PENDING);
                nodes.add(node);
                continue;
            }

            Task active = activeByKey.get(leaf.taskDefinitionKey());
            HistoricTaskInstance historic = latestHistoricByKey.get(leaf.taskDefinitionKey());

            if (active != null) {
                node.setNodeStatus(STATUS_ACTIVE);
                node.setTaskId(active.getId());
                fillAssignee(node, active.getAssignee(), active.getId(), commentsByTaskId, displayNames);
                node.setStartTime(WorkflowMappingSupport.toLocalDateTime(active.getCreateTime()));
                TaskComment comment = commentsByTaskId.get(active.getId());
                node.setComment(comment == null ? null : comment.message());
            } else if (historic != null && historic.getEndTime() != null) {
                Boolean approved = resolveApproved(historic, variables);
                node.setApproved(approved);
                node.setNodeStatus(Boolean.FALSE.equals(approved) ? STATUS_REJECTED : STATUS_COMPLETED);
                node.setTaskId(historic.getId());
                fillAssignee(node, historic.getAssignee(), historic.getId(), commentsByTaskId, displayNames);
                node.setStartTime(WorkflowMappingSupport.toLocalDateTime(historic.getCreateTime()));
                node.setEndTime(WorkflowMappingSupport.toLocalDateTime(historic.getEndTime()));
                TaskComment comment = commentsByTaskId.get(historic.getId());
                node.setComment(comment == null ? null : comment.message());
                if (Boolean.FALSE.equals(approved)) {
                    blockedAfterReject = true;
                }
            } else {
                node.setNodeStatus(STATUS_PENDING);
            }
            nodes.add(node);
        }
        markUnreachedNodesSkipped(designJson, nodes, processEnded);
        return nodes;
    }

    /**
     * 排他条件：兄弟路径已到达时，未到达叶子标为 skipped；
     * 并行/包容：运行中不因兄弟到达而跳过（可能多路同时有效）；
     * 流程已结束时，剩余 pending 一律 skipped。
     */
    private void markUnreachedNodesSkipped(String designJson,
                                           List<WorkflowProgressNodeVO> nodes,
                                           boolean processEnded) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        Map<String, WorkflowProgressNodeVO> byKey = nodes.stream()
                .filter(n -> StringUtils.hasText(n.getNodeKey()))
                .collect(Collectors.toMap(WorkflowProgressNodeVO::getNodeKey, n -> n, (a, b) -> a, LinkedHashMap::new));
        WorkflowGatewaySkipSupport.markExclusiveUnreachedSkipped(
                designSoTSupport.parseDesignNodes(designJson), byKey);
        if (processEnded) {
            for (WorkflowProgressNodeVO node : nodes) {
                if (STATUS_PENDING.equals(node.getNodeStatus())) {
                    node.setNodeStatus(STATUS_SKIPPED);
                }
            }
        }
    }

    private void fillAssignee(WorkflowProgressNodeVO node,
                              String assignee,
                              String taskId,
                              Map<String, TaskComment> commentsByTaskId,
                              Map<String, String> displayNames) {
        String userId = resolveAssigneeUserId(assignee, taskId, commentsByTaskId);
        node.setAssigneeUserId(userId);
        node.setAssigneeUserName(resolveName(displayNames, userId, null));
    }

    private List<UserTask> loadOrderedUserTasks(String processDefinitionId) {
        if (!StringUtils.hasText(processDefinitionId)) {
            return List.of();
        }
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        if (definition == null) {
            return List.of();
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());
        if (bpmnModel == null) {
            return List.of();
        }
        Process process = bpmnModel.getMainProcess();
        if (process == null) {
            return List.of();
        }
        List<UserTask> userTasks = new ArrayList<>();
        for (FlowElement element : process.getFlowElements()) {
            if (element instanceof UserTask userTask) {
                userTasks.add(userTask);
            }
        }
        return userTasks;
    }

    private WorkflowHistoricTaskVO toHistoricVo(HistoricTaskInstance task,
                                                Map<String, Object> variables,
                                                Map<String, TaskComment> commentsByTaskId,
                                                Map<String, String> displayNames) {
        WorkflowHistoricTaskVO vo = new WorkflowHistoricTaskVO();
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getName());
        vo.setTaskDefinitionKey(task.getTaskDefinitionKey());
        String assignee = resolveAssigneeUserId(task.getAssignee(), task.getId(), commentsByTaskId);
        vo.setAssigneeUserId(assignee);
        vo.setAssigneeUserName(resolveName(displayNames, assignee, null));
        vo.setStartTime(WorkflowMappingSupport.toLocalDateTime(task.getCreateTime()));
        vo.setEndTime(WorkflowMappingSupport.toLocalDateTime(task.getEndTime()));
        TaskComment comment = commentsByTaskId.get(task.getId());
        vo.setComment(comment == null ? null : comment.message());
        Boolean approved = resolveApproved(task, variables);
        vo.setApproved(approved);
        return vo;
    }

    private static Boolean resolveApproved(HistoricTaskInstance task, Map<String, Object> variables) {
        if (WorkflowCcTaskSupport.isCcTask(task)) {
            return null;
        }
        Object approved = task.getTaskLocalVariables() == null
                ? null : task.getTaskLocalVariables().get(WorkflowVariables.APPROVED);
        if (!(approved instanceof Boolean) && variables != null) {
            approved = variables.get(WorkflowVariables.APPROVED);
        }
        return approved instanceof Boolean bool ? bool : null;
    }

    private Map<String, TaskComment> loadCommentsByTaskId(String instanceId) {
        List<Comment> comments = taskService.getProcessInstanceComments(instanceId);
        if (CollectionUtils.isEmpty(comments)) {
            return Map.of();
        }
        Map<String, TaskComment> byTaskId = new LinkedHashMap<>();
        comments.stream()
                .filter(comment -> StringUtils.hasText(comment.getTaskId()))
                .sorted(Comparator.comparing(Comment::getTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(comment -> byTaskId.put(comment.getTaskId(),
                        new TaskComment(comment.getFullMessage(), comment.getUserId())));
        return byTaskId;
    }

    private String resolveAssigneeUserId(String assignee,
                                         String taskId,
                                         Map<String, TaskComment> commentsByTaskId) {
        if (StringUtils.hasText(assignee)) {
            return assignee.trim();
        }
        if (StringUtils.hasText(taskId) && commentsByTaskId != null) {
            TaskComment comment = commentsByTaskId.get(taskId);
            if (comment != null && StringUtils.hasText(comment.userId())) {
                return comment.userId().trim();
            }
        }
        if (!StringUtils.hasText(taskId)) {
            return null;
        }
        try {
            List<HistoricIdentityLink> links = historyService.getHistoricIdentityLinksForTask(taskId);
            if (!CollectionUtils.isEmpty(links)) {
                for (HistoricIdentityLink link : links) {
                    if (link != null && "assignee".equalsIgnoreCase(link.getType())
                            && StringUtils.hasText(link.getUserId())) {
                        return link.getUserId().trim();
                    }
                }
            }
        } catch (Exception ignored) {
            // 历史库缺表/旧引擎时忽略，回落 null
        }
        return null;
    }

    private Set<String> collectUserIds(List<HistoricTaskInstance> historicTasks,
                                       List<Task> activeTasks,
                                       Map<String, Object> variables,
                                       Map<String, TaskComment> commentsByTaskId) {
        Set<String> userIds = new HashSet<>();
        for (HistoricTaskInstance task : historicTasks) {
            String assignee = resolveAssigneeUserId(task.getAssignee(), task.getId(), commentsByTaskId);
            if (StringUtils.hasText(assignee)) {
                userIds.add(assignee);
            }
        }
        for (Task task : activeTasks) {
            String assignee = resolveAssigneeUserId(task.getAssignee(), task.getId(), commentsByTaskId);
            if (StringUtils.hasText(assignee)) {
                userIds.add(assignee);
            }
        }
        if (commentsByTaskId != null) {
            for (TaskComment comment : commentsByTaskId.values()) {
                if (comment != null && StringUtils.hasText(comment.userId())) {
                    userIds.add(comment.userId().trim());
                }
            }
        }
        String starter = asString(variables.get(WorkflowVariables.STARTER_USER_ID));
        if (StringUtils.hasText(starter)) {
            userIds.add(starter);
        }
        return userIds;
    }

    private static String resolveName(Map<String, String> displayNames, String userId, String fallback) {
        if (StringUtils.hasText(userId) && displayNames != null) {
            String name = displayNames.get(userId);
            if (StringUtils.hasText(name)) {
                return name;
            }
        }
        if (StringUtils.hasText(fallback)) {
            return fallback;
        }
        return StringUtils.hasText(userId) ? userId : null;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record TaskComment(String message, String userId) {
    }
}
