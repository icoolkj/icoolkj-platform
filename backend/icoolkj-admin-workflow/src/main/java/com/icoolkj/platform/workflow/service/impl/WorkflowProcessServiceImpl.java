package com.icoolkj.platform.workflow.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.framework.datascope.OwnershipColumns;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.workflow.constant.WfBizProcessStatus;
import com.icoolkj.platform.workflow.constant.WfTaskActionTypes;
import com.icoolkj.platform.workflow.constant.WorkflowCancelReasons;
import com.icoolkj.platform.workflow.constant.WorkflowEndEvents;
import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds;
import com.icoolkj.platform.workflow.engine.FlowableProcessDeployService;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.dto.WorkflowModelDesignNodeRequest;
import com.icoolkj.platform.workflow.dto.WorkflowStartRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskAddSignRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskCompleteRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskDelegateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskJumpRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskRemoveSignRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskTransferRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskUrgeRequest;
import com.icoolkj.platform.workflow.event.WorkflowLifecycleNotifyEvent;
import com.icoolkj.platform.workflow.event.WorkflowNotifyPublisher;
import com.icoolkj.platform.workflow.support.WorkflowAccessGuard;
import com.icoolkj.platform.workflow.support.WorkflowBizProcessLookup;
import com.icoolkj.platform.workflow.support.WorkflowCcTaskSupport;
import com.icoolkj.platform.workflow.support.WorkflowDesignNodeLookup;
import com.icoolkj.platform.workflow.support.WorkflowProcessConfigSupport;
import com.icoolkj.platform.workflow.support.WorkflowStartCoordinator;
import com.icoolkj.platform.workflow.support.WorkflowTaskActionRecorder;
import com.icoolkj.platform.workflow.support.WorkflowTitleContract;
import com.icoolkj.platform.workflow.support.WorkflowTitleTemplateSupport;
import com.icoolkj.platform.workflow.support.WorkflowVersionConfigLookup;
import com.icoolkj.platform.workflow.spi.WorkflowNotifyTemplateSource;
import com.icoolkj.platform.framework.security.PermissionAuthorizationService;
import com.icoolkj.platform.workflow.mapper.WfBizProcessMapper;
import com.icoolkj.platform.workflow.service.WorkflowModelRegistry;
import com.icoolkj.platform.workflow.service.WorkflowProcessService;
import com.icoolkj.platform.workflow.spi.WorkflowApproveFormHandler;
import com.icoolkj.platform.workflow.spi.WorkflowBizHandler;
import com.icoolkj.platform.workflow.spi.WorkflowOperatorContextResolver;
import com.icoolkj.platform.workflow.spi.WorkflowRuntimeVariableEnricher;
import com.icoolkj.platform.workflow.reject.RejectModeContext;
import com.icoolkj.platform.workflow.reject.RejectModeHandler;
import com.icoolkj.platform.workflow.reject.RejectModeHandlerRegistry;
import com.icoolkj.platform.workflow.support.WorkflowNodeFieldPermSupport;
import com.icoolkj.platform.workflow.support.WorkflowPassModes;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Flowable 流程编排实现 */
@Service
public class WorkflowProcessServiceImpl implements WorkflowProcessService {

    public static final String EVENT_REJECT = "REJECT";
    public static final String EVENT_COMPLETE = "COMPLETE";
    public static final String EVENT_RETURN = "RETURN";
    public static final String EVENT_TODO = "TODO";
    public static final String EVENT_CC = "CC";
    public static final String EVENT_URGE = "URGE";

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final IdentityService identityService;
    private final RepositoryService repositoryService;
    private final List<WorkflowBizHandler> bizHandlers;
    private final List<WorkflowApproveFormHandler> approveFormHandlers;
    private final List<WorkflowRuntimeVariableEnricher> runtimeVariableEnrichers;
    private final WfBizProcessMapper wfBizProcessMapper;
    private final WorkflowModelRegistry workflowModelRegistry;
    private final WorkflowStartCoordinator workflowStartCoordinator;
    private final WorkflowAccessGuard workflowAccessGuard;
    private final FlowableProcessDeployService processDeployService;
    private final WorkflowBizProcessLookup workflowBizProcessLookup;
    private final WorkflowProcessConfigSupport processConfigSupport;
    private final ObjectProvider<WorkflowOperatorContextResolver> operatorContextResolver;
    private final WorkflowNotifyPublisher notifyPublisher;
    private final I18nMessageResolver i18nMessageResolver;
    private final WorkflowNotifyTemplateSource notifyTemplateSource;
    private final WorkflowTaskActionRecorder taskActionRecorder;
    private final WorkflowDesignNodeLookup designNodeLookup;
    private final WorkflowVersionConfigLookup versionConfigLookup;
    private final PermissionAuthorizationService authz;
    private final RejectModeHandlerRegistry rejectModeHandlerRegistry;

    public WorkflowProcessServiceImpl(RuntimeService runtimeService,
                                      TaskService taskService,
                                      HistoryService historyService,
                                      IdentityService identityService,
                                      RepositoryService repositoryService,
                                      List<WorkflowBizHandler> bizHandlers,
                                      List<WorkflowApproveFormHandler> approveFormHandlers,
                                      List<WorkflowRuntimeVariableEnricher> runtimeVariableEnrichers,
                                      WfBizProcessMapper wfBizProcessMapper,
                                      WorkflowModelRegistry workflowModelRegistry,
                                      WorkflowStartCoordinator workflowStartCoordinator,
                                      WorkflowAccessGuard workflowAccessGuard,
                                      FlowableProcessDeployService processDeployService,
                                      WorkflowBizProcessLookup workflowBizProcessLookup,
                                      WorkflowProcessConfigSupport processConfigSupport,
                                      ObjectProvider<WorkflowOperatorContextResolver> operatorContextResolver,
                                      ObjectProvider<WorkflowNotifyPublisher> notifyPublisherProvider,
                                      ApplicationEventPublisher eventPublisher,
                                      I18nMessageResolver i18nMessageResolver,
                                      WorkflowNotifyTemplateSource notifyTemplateSource,
                                      WorkflowTaskActionRecorder taskActionRecorder,
                                      WorkflowDesignNodeLookup designNodeLookup,
                                      WorkflowVersionConfigLookup versionConfigLookup,
                                      PermissionAuthorizationService authz,
                                      RejectModeHandlerRegistry rejectModeHandlerRegistry) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
        this.identityService = identityService;
        this.repositoryService = repositoryService;
        this.bizHandlers = bizHandlers == null ? List.of() : bizHandlers;
        this.approveFormHandlers = approveFormHandlers == null ? List.of() : approveFormHandlers;
        this.runtimeVariableEnrichers = runtimeVariableEnrichers == null ? List.of() : runtimeVariableEnrichers;
        this.wfBizProcessMapper = wfBizProcessMapper;
        this.workflowModelRegistry = workflowModelRegistry;
        this.workflowStartCoordinator = workflowStartCoordinator;
        this.workflowAccessGuard = workflowAccessGuard;
        this.processDeployService = processDeployService;
        this.workflowBizProcessLookup = workflowBizProcessLookup;
        this.processConfigSupport = processConfigSupport;
        this.operatorContextResolver = operatorContextResolver;
        WorkflowNotifyPublisher custom = notifyPublisherProvider.getIfAvailable();
        this.notifyPublisher = custom != null ? custom : eventPublisher::publishEvent;
        this.i18nMessageResolver = i18nMessageResolver;
        this.notifyTemplateSource = notifyTemplateSource;
        this.taskActionRecorder = taskActionRecorder;
        this.designNodeLookup = designNodeLookup;
        this.versionConfigLookup = versionConfigLookup;
        this.authz = authz;
        this.rejectModeHandlerRegistry = rejectModeHandlerRegistry;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startProcess(WorkflowStartRequest request) {
        String starterUserId = requireUserId();
        WfModelEntity model = resolveModel(request);
        assertStartAcl(model, starterUserId);
        String bizTenantId = WorkflowStartCoordinator.requireBizTenantId(request.getTenantId());
        if (!processDeployService.hasDefinition(model.getModelKey(), model.getTenantId())) {
            throw new BusinessException("error.workflow.model_not_deployed");
        }
        String flowableTenantId = processDeployService.resolveFlowableTenantId(model.getModelKey(), model.getTenantId());
        LocalDateTime startTime = LocalDateTime.now();
        String bizTitle = resolveBizTitle(request, model, startTime);
        if (!StringUtils.hasText(request.getBizTitle())) {
            request.setBizTitle(bizTitle);
        }
        Map<String, Object> variables = workflowStartCoordinator.buildStartVariables(request, model, starterUserId);

        identityService.setAuthenticatedUserId(starterUserId);
        ProcessInstance instance;
        try {
            var builder = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey(model.getModelKey())
                    .businessKey(request.getBizId())
                    .variables(variables);
            if (flowableTenantId != null) {
                builder.tenantId(flowableTenantId);
            }
            instance = builder.start();
        } finally {
            identityService.setAuthenticatedUserId(null);
        }

        WfBizProcessEntity bizProcess = new WfBizProcessEntity();
        bizProcess.setId(IdGeneratorUtils.nextIdStr());
        bizProcess.setTenantId(bizTenantId);
        bizProcess.setModelId(model.getId());
        bizProcess.setModelVersionNo(model.getLatestVersion());
        bizProcess.setBizType(request.getBizType());
        bizProcess.setBizId(request.getBizId());
        bizProcess.setBizTitle(bizTitle);
        bizProcess.setProcessDefinitionKey(model.getModelKey());
        bizProcess.setProcessInstanceId(instance.getId());
        bizProcess.setStatus(WfBizProcessStatus.RUNNING);
        bizProcess.setStarterUserId(starterUserId);
        applyOwnership(bizProcess, starterUserId);
        bizProcess.setStartTime(startTime);
        AuditFieldHelper.fillInsert(bizProcess);
        wfBizProcessMapper.insert(bizProcess);
        notifyActiveAssignees(instance.getId(), bizProcess, variables);
        return instance.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, WorkflowTaskCompleteRequest request) {
        Task task = requireAssignableTask(taskId);
        assertDelegateNotExpired(task);
        boolean approved = Boolean.TRUE.equals(request.getApproved());
        boolean ccTask = isCcTask(task);
        if (!ccTask && !approved && !StringUtils.hasText(request.getComment())) {
            throw new BusinessException("error.workflow.comment_required_on_reject");
        }
        WfBizProcessEntity biz = workflowBizProcessLookup.findByProcessInstanceId(task.getProcessInstanceId());
        if (!ccTask && biz != null && StringUtils.hasText(biz.getModelId())) {
            var behavior = configForInstance(task.getProcessInstanceId(), biz.getModelId()).behavior();
            if (behavior != null && Boolean.TRUE.equals(behavior.getCommentRequired())
                    && !StringUtils.hasText(request.getComment())) {
                throw new BusinessException("error.workflow.comment_required");
            }
        }
        String operatorUserId = requireUserId();
        identityService.setAuthenticatedUserId(operatorUserId);
        try {
            // DelegateTask#setAssignee 不写历史；提交前用 TaskService 同步 ACT_HI_TASKINST.ASSIGNEE_
            taskService.setAssignee(taskId, operatorUserId);
        if (StringUtils.hasText(request.getComment())) {
            taskService.addComment(taskId, task.getProcessInstanceId(), request.getComment());
        }

            if (!ccTask && request.getFormData() != null && !request.getFormData().isEmpty()) {
                applyApproveFormPatch(task, biz, request.getFormData());
            }

            // 委派归还：被委派人办理后交回原办理人（结束旧任务落已办，再创建原办理人待办）
            if (!ccTask && approved && resolveDelegateReturn(task, biz, operatorUserId, request.getComment())) {
                return;
            }

            // 前加签归还：加签人通过后同节点交接回原办理人（结束旧任务以落已办历史）
            if (!ccTask && approved) {
                Object returnUser = runtimeService.getVariable(
                        task.getProcessInstanceId(), WorkflowVariables.ADD_SIGN_RETURN_USER_ID);
                if (returnUser != null && StringUtils.hasText(String.valueOf(returnUser))) {
                    String original = String.valueOf(returnUser).trim();
                    runtimeService.removeVariable(task.getProcessInstanceId(), WorkflowVariables.ADD_SIGN_RETURN_USER_ID);
                    String oldTaskId = task.getId();
                    handOffSameNodeToUser(task, operatorUserId, original);
                    taskActionRecorder.record(
                            biz == null ? null : biz.getTenantId(),
                            task.getProcessInstanceId(),
                            oldTaskId,
                            task.getTaskDefinitionKey(),
                            WfTaskActionTypes.ADD_SIGN_BEFORE,
                            operatorUserId,
                            original,
                            request.getComment(),
                            "{\"phase\":\"" + WfTaskActionTypes.PHASE_RETURN + "\"}");
                    notifyActiveAssignees(task.getProcessInstanceId(), biz, getProcessVariables(task.getProcessInstanceId()));
                    return;
                }
            }

            // 后加签交接：当前人通过后同节点转给加签人（禁止直接 complete，末节点会结束流程）
            if (!ccTask && approved && handOffAfterAddSign(task, biz, operatorUserId, request.getComment())) {
                return;
            }

            if (!ccTask && !approved) {
                WorkflowModelDesignNodeRequest designNode = designNodeLookup.findByProcessInstanceAndTaskKey(
                        task.getProcessInstanceId(), task.getTaskDefinitionKey());
                String rejectMode = WorkflowDesignNodeLookup.rejectModeOf(designNode);
                if (!"end".equals(rejectMode)
                        && tryRejectJump(task, biz, rejectMode, request.getComment())) {
                    return;
                }
            }

            if (!ccTask) {
                enrichRuntimeVariables(biz, task.getProcessInstanceId());
            }

            Map<String, Object> completeVars = ccTask
                    ? Map.of()
                    : Map.of(WorkflowVariables.APPROVED, approved);
            if (!ccTask) {
        taskService.setVariableLocal(taskId, WorkflowVariables.APPROVED, approved);
            }
            taskService.complete(taskId, completeVars);

        String processInstanceId = task.getProcessInstanceId();
        syncBizStatusIfEnded(processInstanceId);
        ProcessInstance stillRunning = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (stillRunning != null) {
            Map<String, Object> variables = getProcessVariables(processInstanceId);
            notifyActiveAssignees(processInstanceId, biz, variables);
        }
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferTask(String taskId, WorkflowTaskTransferRequest request) {
        Task task = requireAssignableTask(taskId);
        if (isCcTask(task)) {
            throw new BusinessException("error.workflow.cc_action_forbidden");
        }
        if (!StringUtils.hasText(request.getTargetUserId())) {
            throw new BusinessException("error.workflow.transfer_target_required");
        }
        String target = request.getTargetUserId().trim();
        if (target.equals(task.getAssignee())) {
            return;
        }
        WorkflowModelDesignNodeRequest designNode = designNodeLookup.findByProcessInstanceAndTaskKey(
                task.getProcessInstanceId(), task.getTaskDefinitionKey());
        if (!WorkflowDesignNodeLookup.allowTransfer(designNode)) {
            throw new BusinessException("error.workflow.transfer_not_allowed");
        }
        WfBizProcessEntity biz = workflowBizProcessLookup.findByProcessInstanceId(task.getProcessInstanceId());
        String from = task.getAssignee();
        String operatorId = requireUserId();
        identityService.setAuthenticatedUserId(operatorId);
        try {
            if (StringUtils.hasText(request.getComment())) {
                taskService.addComment(taskId, task.getProcessInstanceId(), request.getComment());
            }
            taskService.setAssignee(taskId, target);
            // 同步节点办理人变量，避免驳回重入后表达式仍指向原办理人
            String varName = designNode != null && StringUtils.hasText(designNode.getVariableName())
                    ? designNode.getVariableName().trim()
                    : task.getTaskDefinitionKey() + "UserId";
            runtimeService.setVariable(task.getProcessInstanceId(), varName, target);
            taskActionRecorder.record(
                    biz == null ? null : biz.getTenantId(),
                    task.getProcessInstanceId(),
                    taskId,
                    task.getTaskDefinitionKey(),
                    WfTaskActionTypes.TRANSFER,
                    from,
                    target,
                    request.getComment(),
                    null);
            Map<String, Object> variables = getProcessVariables(task.getProcessInstanceId());
            publishLifecycleNotify(EVENT_TODO, variables, task.getProcessInstanceId(), biz,
                    List.of(target), WorkflowProcessConfigSupport.Notify::isOnTodo);
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(String taskId, WorkflowTaskDelegateRequest request) {
        Task task = requireAssignableTask(taskId);
        if (isCcTask(task)) {
            throw new BusinessException("error.workflow.cc_action_forbidden");
        }
        if (!StringUtils.hasText(request.getTargetUserId())) {
            throw new BusinessException("error.workflow.delegate_target_required");
        }
        String target = request.getTargetUserId().trim();
        if (target.equals(task.getAssignee())) {
            return;
        }
        WorkflowModelDesignNodeRequest designNode = designNodeLookup.findByProcessInstanceAndTaskKey(
                task.getProcessInstanceId(), task.getTaskDefinitionKey());
        if (!WorkflowDesignNodeLookup.allowDelegate(designNode)) {
            throw new BusinessException("error.workflow.delegate_not_allowed");
        }
        WfBizProcessEntity biz = workflowBizProcessLookup.findByProcessInstanceId(task.getProcessInstanceId());
        String from = task.getAssignee();
        String operatorId = requireUserId();
        identityService.setAuthenticatedUserId(operatorId);
        try {
            if (StringUtils.hasText(request.getComment())) {
                taskService.addComment(taskId, task.getProcessInstanceId(), request.getComment());
            }
            taskService.setVariableLocal(taskId, WorkflowVariables.DELEGATE_OWNER_USER_ID, from);
            if (request.getDelegateUntil() != null) {
                taskService.setVariableLocal(taskId, WorkflowVariables.DELEGATE_UNTIL,
                        request.getDelegateUntil().toString());
            } else {
                taskService.removeVariableLocal(taskId, WorkflowVariables.DELEGATE_UNTIL);
            }
            taskService.setAssignee(taskId, target);
            String varName = designNode != null && StringUtils.hasText(designNode.getVariableName())
                    ? designNode.getVariableName().trim()
                    : task.getTaskDefinitionKey() + "UserId";
            runtimeService.setVariable(task.getProcessInstanceId(), varName, target);
            String payload = buildDelegatePayload(from, request.getDelegateUntil());
            taskActionRecorder.record(
                    biz == null ? null : biz.getTenantId(),
                    task.getProcessInstanceId(),
                    taskId,
                    task.getTaskDefinitionKey(),
                    WfTaskActionTypes.DELEGATE,
                    from,
                    target,
                    request.getComment(),
                    payload);
            Map<String, Object> variables = getProcessVariables(task.getProcessInstanceId());
            publishLifecycleNotify(EVENT_TODO, variables, task.getProcessInstanceId(), biz,
                    List.of(target), WorkflowProcessConfigSupport.Notify::isOnTodo);
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void jumpTask(String taskId, WorkflowTaskJumpRequest request) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("error.workflow.task_not_found");
        }
        if (isCcTask(task)) {
            throw new BusinessException("error.workflow.cc_action_forbidden");
        }
        if (!StringUtils.hasText(request.getTargetActivityId())) {
            throw new BusinessException("error.workflow.jump_target_required");
        }
        String targetKey = request.getTargetActivityId().trim();
        String pi = task.getProcessInstanceId();
        String currentKey = task.getTaskDefinitionKey();
        if (targetKey.equals(currentKey)) {
            return;
        }
        assertJumpTargetValid(task, targetKey);
        WfBizProcessEntity biz = workflowBizProcessLookup.findByProcessInstanceId(pi);
        String operatorId = requireUserId();
        identityService.setAuthenticatedUserId(operatorId);
        try {
            if (StringUtils.hasText(request.getComment())) {
                taskService.addComment(taskId, pi, request.getComment());
            }
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(pi)
                    .moveActivityIdTo(currentKey, targetKey)
                    .changeState();
            taskActionRecorder.record(
                    biz == null ? null : biz.getTenantId(),
                    pi,
                    taskId,
                    currentKey,
                    WfTaskActionTypes.JUMP,
                    operatorId,
                    null,
                    request.getComment(),
                    "{\"target\":\"" + targetKey + "\"}");
            Map<String, Object> variables = getProcessVariables(pi);
            notifyActiveAssignees(pi, biz, variables);
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void urgeTask(String taskId, WorkflowTaskUrgeRequest request) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("error.workflow.task_not_found");
        }
        if (isCcTask(task)) {
            throw new BusinessException("error.workflow.cc_action_forbidden");
        }
        WfBizProcessEntity biz = workflowBizProcessLookup.findByProcessInstanceId(task.getProcessInstanceId());
        if (biz == null) {
            throw new BusinessException("error.workflow.task_not_found");
        }
        String userId = requireUserId();
        boolean starter = userId.equals(biz.getStarterUserId());
        if (!starter && !authz.hasPermission("workflow:task:urge")) {
            throw new BusinessException("error.workflow.urge_not_allowed");
        }
        var notify = configForInstance(task.getProcessInstanceId(), biz.getModelId()).notifyConfig();
        if (!notify.isOnUrge()) {
            throw new BusinessException("error.workflow.urge_disabled");
        }
        List<String> recipients = new ArrayList<>();
        if (StringUtils.hasText(task.getAssignee())) {
            recipients.add(task.getAssignee());
        }
        if (recipients.isEmpty()) {
            return;
        }
        identityService.setAuthenticatedUserId(userId);
        try {
            if (StringUtils.hasText(request.getComment())) {
                taskService.addComment(taskId, task.getProcessInstanceId(), request.getComment());
            }
            Map<String, Object> variables = getProcessVariables(task.getProcessInstanceId());
            publishLifecycleNotify(EVENT_URGE, variables, task.getProcessInstanceId(), biz,
                    recipients, n -> true);
            taskActionRecorder.record(
                    biz.getTenantId(),
                    task.getProcessInstanceId(),
                    taskId,
                    task.getTaskDefinitionKey(),
                    WfTaskActionTypes.URGE,
                    userId,
                    task.getAssignee(),
                    request.getComment(),
                    null);
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSignTask(String taskId, WorkflowTaskAddSignRequest request) {
        Task task = requireAssignableTask(taskId);
        if (isCcTask(task)) {
            throw new BusinessException("error.workflow.cc_action_forbidden");
        }
        WorkflowModelDesignNodeRequest designNode = designNodeLookup.findByProcessInstanceAndTaskKey(
                task.getProcessInstanceId(), task.getTaskDefinitionKey());
        if (!WorkflowDesignNodeLookup.allowAddSign(designNode)) {
            throw new BusinessException("error.workflow.add_sign_not_allowed");
        }
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            throw new BusinessException("error.workflow.add_sign_users_required");
        }
        String mode = request.getMode() == null ? "" : request.getMode().trim().toLowerCase();
        if (!"before".equals(mode) && !"after".equals(mode)) {
            throw new BusinessException("error.workflow.add_sign_not_allowed");
        }
        List<String> userIds = request.getUserIds().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            throw new BusinessException("error.workflow.add_sign_users_required");
        }
        WfBizProcessEntity biz = workflowBizProcessLookup.findByProcessInstanceId(task.getProcessInstanceId());
        String actionType = "before".equals(mode)
                ? WfTaskActionTypes.ADD_SIGN_BEFORE
                : WfTaskActionTypes.ADD_SIGN_AFTER;
        String operatorId = requireUserId();
        identityService.setAuthenticatedUserId(operatorId);
        try {
            if (StringUtils.hasText(request.getComment())) {
                taskService.addComment(taskId, task.getProcessInstanceId(), request.getComment());
            }

            // Flowable MI：运行时加人；否则用 sequential 前/后加签变量策略
            boolean multi = designNode != null && WorkflowPassModes.isMultiInstance(designNode.getPassMode());

            if (multi) {
                String varName = designNode != null && StringUtils.hasText(designNode.getVariableName())
                        ? designNode.getVariableName().trim()
                        : task.getTaskDefinitionKey() + "UserId";
                for (String uid : userIds) {
                    runtimeService.addMultiInstanceExecution(
                            task.getTaskDefinitionKey(),
                            task.getProcessInstanceId(),
                            Map.of(varName, uid));
                }
            } else if ("before".equals(mode)) {
                String original = task.getAssignee();
                runtimeService.setVariable(task.getProcessInstanceId(),
                        WorkflowVariables.ADD_SIGN_RETURN_USER_ID, original);
                taskService.setAssignee(taskId, userIds.get(0));
            } else {
                runtimeService.setVariable(task.getProcessInstanceId(),
                        WorkflowVariables.ADD_SIGN_AFTER_USER_IDS, String.join(",", userIds));
            }

            taskActionRecorder.record(
                    biz == null ? null : biz.getTenantId(),
                    task.getProcessInstanceId(),
                    taskId,
                    task.getTaskDefinitionKey(),
                    actionType,
                    operatorId,
                    userIds.get(0),
                    request.getComment(),
                    "{\"userIds\":[\"" + String.join("\",\"", userIds) + "\"]}");
            notifyActiveAssignees(task.getProcessInstanceId(), biz, getProcessVariables(task.getProcessInstanceId()));
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSignTask(String taskId, WorkflowTaskRemoveSignRequest request) {
        Task task = requireAssignableTask(taskId);
        if (isCcTask(task)) {
            throw new BusinessException("error.workflow.cc_action_forbidden");
        }
        WorkflowModelDesignNodeRequest designNode = designNodeLookup.findByProcessInstanceAndTaskKey(
                task.getProcessInstanceId(), task.getTaskDefinitionKey());
        if (!WorkflowDesignNodeLookup.allowAddSign(designNode)) {
            throw new BusinessException("error.workflow.remove_sign_not_allowed");
        }
        boolean multi = designNode != null && WorkflowPassModes.isMultiInstance(designNode.getPassMode());
        if (!multi) {
            throw new BusinessException("error.workflow.remove_sign_not_multi");
        }
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            throw new BusinessException("error.workflow.remove_sign_users_required");
        }
        List<String> userIds = request.getUserIds().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            throw new BusinessException("error.workflow.remove_sign_users_required");
        }
        String operatorId = requireUserId();
        for (String uid : userIds) {
            if (uid.equals(operatorId) || uid.equals(task.getAssignee())) {
                throw new BusinessException("error.workflow.remove_sign_self_forbidden");
            }
        }

        List<Task> siblings = taskService.createTaskQuery()
                .processInstanceId(task.getProcessInstanceId())
                .taskDefinitionKey(task.getTaskDefinitionKey())
                .active()
                .list();
        if (siblings.size() - userIds.size() < 1) {
            throw new BusinessException("error.workflow.remove_sign_last_assignee");
        }

        Map<String, Task> byAssignee = new java.util.LinkedHashMap<>();
        for (Task sibling : siblings) {
            if (StringUtils.hasText(sibling.getAssignee()) && !sibling.getId().equals(taskId)) {
                byAssignee.putIfAbsent(sibling.getAssignee(), sibling);
            }
        }
        List<Task> toRemove = new ArrayList<>();
        for (String uid : userIds) {
            Task target = byAssignee.get(uid);
            if (target == null) {
                throw new BusinessException("error.workflow.remove_sign_user_not_found");
            }
            toRemove.add(target);
        }

        identityService.setAuthenticatedUserId(operatorId);
        try {
            if (StringUtils.hasText(request.getComment())) {
                taskService.addComment(taskId, task.getProcessInstanceId(), request.getComment());
            }
            for (Task target : toRemove) {
                runtimeService.deleteMultiInstanceExecution(target.getExecutionId(), false);
            }

            WfBizProcessEntity biz = workflowBizProcessLookup.findByProcessInstanceId(task.getProcessInstanceId());
            taskActionRecorder.record(
                    biz == null ? null : biz.getTenantId(),
                    task.getProcessInstanceId(),
                    taskId,
                    task.getTaskDefinitionKey(),
                    WfTaskActionTypes.REMOVE_SIGN,
                    operatorId,
                    userIds.get(0),
                    request.getComment(),
                    "{\"userIds\":[\"" + String.join("\",\"", userIds) + "\"]}");
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    private void assertDelegateNotExpired(Task task) {
        if (task == null) {
            return;
        }
        Object raw = taskService.getVariableLocal(task.getId(), WorkflowVariables.DELEGATE_UNTIL);
        if (raw == null || !StringUtils.hasText(String.valueOf(raw))) {
            return;
        }
        LocalDateTime until = parseDelegateUntil(String.valueOf(raw).trim());
        if (until != null && LocalDateTime.now().isAfter(until)) {
            throw new BusinessException("error.workflow.delegate_expired");
        }
    }

    private static LocalDateTime parseDelegateUntil(String raw) {
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ignored) {
            // ignore
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(raw), ZoneId.systemDefault());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String buildDelegatePayload(String ownerUserId, LocalDateTime delegateUntil) {
        StringBuilder sb = new StringBuilder("{\"owner\":\"");
        sb.append(ownerUserId == null ? "" : ownerUserId.replace("\"", "\\\""));
        sb.append("\"");
        if (delegateUntil != null) {
            sb.append(",\"delegateUntil\":\"").append(delegateUntil).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private void assertJumpTargetValid(Task task, String targetActivityId) {
        BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        if (model == null || model.getMainProcess() == null) {
            throw new BusinessException("error.workflow.jump_target_invalid");
        }
        FlowElement element = model.getMainProcess().getFlowElement(targetActivityId);
        if (element == null) {
            throw new BusinessException("error.workflow.jump_target_invalid");
        }
        if (!(element instanceof UserTask)) {
            throw new BusinessException("error.workflow.jump_target_invalid");
        }
    }

    private boolean tryRejectJump(Task task, WfBizProcessEntity biz, String rejectMode, String comment) {
        RejectModeHandler handler = rejectModeHandlerRegistry.find(rejectMode);
        if (handler == null) {
            return false;
        }
        RejectModeContext ctx = new RejectModeContext() {
            @Override
            public RuntimeService runtimeService() {
                return runtimeService;
            }

            @Override
            public TaskService taskService() {
                return taskService;
            }

            @Override
            public void afterRejectJump(Task t, WfBizProcessEntity b, String mode, String targetKey, String cmt) {
                String pi = t.getProcessInstanceId();
                taskActionRecorder.record(
                        b == null ? null : b.getTenantId(),
                        pi,
                        t.getId(),
                        t.getTaskDefinitionKey(),
                        WfTaskActionTypes.REJECT_JUMP,
                        requireUserId(),
                        null,
                        cmt,
                        "{\"rejectMode\":\"" + mode + "\",\"target\":\"" + targetKey + "\"}");
                Map<String, Object> variables = getProcessVariables(pi);
                publishLifecycleNotify(EVENT_RETURN, variables, pi, b, List.of(),
                        WorkflowProcessConfigSupport.Notify::isOnReturn);
                dispatchReturned(variables, pi, mode);
                if ("starterEdit".equals(mode)) {
                    updateBizProcessStatus(pi, WfBizProcessStatus.RETURNED);
                } else {
                    notifyActiveAssignees(pi, b, variables);
                }
            }
        };
        return handler.handle(task, biz, comment, ctx);
    }

    /**
     * 被委派人通过后归还原办理人；原办理人再办结。
     *
     * @return true=已归还并应中止本次 complete
     */
    private boolean resolveDelegateReturn(Task task, WfBizProcessEntity biz,
                                          String operatorUserId, String comment) {
        Object ownerRaw = taskService.getVariableLocal(task.getId(), WorkflowVariables.DELEGATE_OWNER_USER_ID);
        if (ownerRaw == null || !StringUtils.hasText(String.valueOf(ownerRaw))) {
            return false;
        }
        String owner = String.valueOf(ownerRaw).trim();
        if (!StringUtils.hasText(owner) || owner.equals(operatorUserId)) {
            return false;
        }
        WorkflowModelDesignNodeRequest designNode = designNodeLookup.findByProcessInstanceAndTaskKey(
                task.getProcessInstanceId(), task.getTaskDefinitionKey());
        String varName = designNode != null && StringUtils.hasText(designNode.getVariableName())
                ? designNode.getVariableName().trim()
                : task.getTaskDefinitionKey() + "UserId";
        String oldTaskId = task.getId();
        handOffSameNodeToUser(task, operatorUserId, owner);
        runtimeService.setVariable(task.getProcessInstanceId(), varName, owner);
        taskActionRecorder.record(
                biz == null ? null : biz.getTenantId(),
                task.getProcessInstanceId(),
                oldTaskId,
                task.getTaskDefinitionKey(),
                WfTaskActionTypes.DELEGATE,
                operatorUserId,
                owner,
                comment,
                "{\"phase\":\"" + WfTaskActionTypes.PHASE_RETURN + "\"}");
        notifyActiveAssignees(task.getProcessInstanceId(), biz, getProcessVariables(task.getProcessInstanceId()));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resubmitFromStarterEdit(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            throw new BusinessException("error.workflow.instance_not_found");
        }
        WfBizProcessEntity biz = workflowBizProcessLookup.requireByProcessInstanceId(processInstanceId);
        String userId = requireUserId();
        if (!userId.equals(biz.getStarterUserId())) {
            throw new BusinessException("error.workflow.not_starter");
        }
        Object pending = runtimeService.getVariable(processInstanceId, WorkflowVariables.STARTER_EDIT_PENDING);
        if (!Boolean.TRUE.equals(pending) && !"true".equalsIgnoreCase(String.valueOf(pending))) {
            throw new BusinessException("error.workflow.starter_edit_not_pending");
        }
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (pi == null) {
            throw new BusinessException("error.workflow.instance_not_running");
        }
        if (pi.isSuspended()) {
            runtimeService.activateProcessInstanceById(processInstanceId);
        }
        runtimeService.removeVariable(processInstanceId, WorkflowVariables.STARTER_EDIT_PENDING);
        updateBizProcessStatus(processInstanceId, WfBizProcessStatus.RUNNING);
        Map<String, Object> variables = getProcessVariables(processInstanceId);
        notifyActiveAssignees(processInstanceId, biz, variables);
    }

    /**
     * 顺序节点后加签：当前办理人通过后同节点交接给加签人。
     * 使用 changeActivityState 结束旧任务（落 ACT_HI_TASKINST / 我已处理），再创建新待办；
     * 避免直接 complete 导致末节点流程结束。
     *
     * @return true=已交接并应中止本次 complete
     */
    private boolean handOffAfterAddSign(Task task, WfBizProcessEntity biz, String operatorUserId, String comment) {
        String pi = task.getProcessInstanceId();
        Object raw = runtimeService.getVariable(pi, WorkflowVariables.ADD_SIGN_AFTER_USER_IDS);
        if (raw == null || !StringUtils.hasText(String.valueOf(raw))) {
            return false;
        }
        String[] parts = String.valueOf(raw).trim().split(",");
        List<String> remaining = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                remaining.add(part.trim());
            }
        }
        if (remaining.isEmpty()) {
            runtimeService.removeVariable(pi, WorkflowVariables.ADD_SIGN_AFTER_USER_IDS);
            return false;
        }
        String next = remaining.remove(0);
        if (remaining.isEmpty()) {
            runtimeService.removeVariable(pi, WorkflowVariables.ADD_SIGN_AFTER_USER_IDS);
        } else {
            runtimeService.setVariable(pi, WorkflowVariables.ADD_SIGN_AFTER_USER_IDS, String.join(",", remaining));
        }
        String oldTaskId = task.getId();
        handOffSameNodeToUser(task, operatorUserId, next);
        taskActionRecorder.record(
                biz == null ? null : biz.getTenantId(),
                pi,
                oldTaskId,
                task.getTaskDefinitionKey(),
                WfTaskActionTypes.ADD_SIGN_AFTER,
                operatorUserId,
                next,
                comment,
                "{\"phase\":\"" + WfTaskActionTypes.PHASE_HANDOFF + "\",\"next\":\"" + next + "\"}");
        notifyActiveAssignees(pi, biz, getProcessVariables(pi));
        return true;
    }

    /**
     * 同节点交接：把当前办理人的任务记为已通过并结束，再在同一 activity 生成新待办给 nextUserId。
     */
    private void handOffSameNodeToUser(Task task, String fromUserId, String nextUserId) {
        if (task == null || !StringUtils.hasText(nextUserId)) {
            throw new BusinessException("error.workflow.task_not_found");
        }
        String activityId = task.getTaskDefinitionKey();
        String pi = task.getProcessInstanceId();
        String executionId = task.getExecutionId();
        if (!StringUtils.hasText(activityId) || !StringUtils.hasText(executionId)) {
            throw new BusinessException("error.workflow.task_not_found");
        }
        if (StringUtils.hasText(fromUserId) && !fromUserId.equals(task.getAssignee())) {
            taskService.setAssignee(task.getId(), fromUserId);
        }
        taskService.setVariableLocal(task.getId(), WorkflowVariables.APPROVED, true);
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(pi)
                .moveSingleExecutionToActivityIds(executionId, List.of(activityId))
                .changeState();
        Task newTask = taskService.createTaskQuery()
                .processInstanceId(pi)
                .taskDefinitionKey(activityId)
                .active()
                .orderByTaskCreateTime()
                .desc()
                .list()
                .stream()
                .findFirst()
                .orElse(null);
        if (newTask == null) {
            throw new BusinessException("error.workflow.task_not_found");
        }
        taskService.setAssignee(newTask.getId(), nextUserId.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProcess(String processInstanceId, String reason) {
        String userId = requireUserId();
        WfBizProcessEntity bizProcess = workflowBizProcessLookup.findByProcessInstanceId(processInstanceId);
        workflowAccessGuard.assertCanCancel(userId, bizProcess);
        assertWithdrawAllowed(bizProcess);
        Map<String, Object> variables = getProcessVariables(processInstanceId);
        String reasonCode = WorkflowCancelReasons.resolveCode(reason);
        String auditComment = null;
        if (reasonCode == null) {
            // 调用方传入自由意见
            reasonCode = WorkflowCancelReasons.DEFAULT;
            auditComment = StringUtils.hasText(reason) ? reason.trim() : null;
        }
        String deleteReason = StringUtils.hasText(auditComment) ? auditComment : reasonCode;
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
        updateBizProcessStatus(processInstanceId, WfBizProcessStatus.CANCELLED);
        // 系统撤回：comment 留空，reasonCode 供前端 gen i18n；自由意见写入 comment
        taskActionRecorder.record(
                bizProcess == null ? null : bizProcess.getTenantId(),
                processInstanceId,
                null,
                null,
                WfTaskActionTypes.WITHDRAW,
                userId,
                null,
                auditComment,
                "{\"status\":\"cancelled\",\"reasonCode\":\"" + reasonCode + "\"}");
        dispatchCancelled(variables, processInstanceId);
        publishLifecycleNotify(EVENT_RETURN, variables, processInstanceId, bizProcess,
                List.of(), n -> n.isOnReturn());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminateProcess(String processInstanceId, String reason) {
        String userId = requireUserId();
        WfBizProcessEntity bizProcess = workflowBizProcessLookup.findByProcessInstanceId(processInstanceId);
        workflowAccessGuard.assertCanTerminate(userId, bizProcess);
        Map<String, Object> variables = getProcessVariables(processInstanceId);
        String reasonCode = WorkflowCancelReasons.resolveCode(reason);
        String auditComment = null;
        if (reasonCode == null) {
            reasonCode = WorkflowCancelReasons.DEFAULT;
            auditComment = StringUtils.hasText(reason) ? reason.trim() : null;
        }
        String deleteReason = StringUtils.hasText(auditComment) ? auditComment : reasonCode;
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
        updateBizProcessStatus(processInstanceId, WfBizProcessStatus.CANCELLED);
        taskActionRecorder.record(
                bizProcess == null ? null : bizProcess.getTenantId(),
                processInstanceId,
                null,
                null,
                WfTaskActionTypes.TERMINATE,
                userId,
                null,
                auditComment,
                "{\"status\":\"cancelled\",\"reasonCode\":\"" + reasonCode + "\",\"source\":\"admin\"}");
        dispatchCancelled(variables, processInstanceId);
        publishLifecycleNotify(EVENT_RETURN, variables, processInstanceId, bizProcess,
                List.of(), n -> n.isOnReturn());
    }

    private void assertWithdrawAllowed(WfBizProcessEntity bizProcess) {
        if (bizProcess == null || !StringUtils.hasText(bizProcess.getModelId())) {
            return;
        }
        var behavior = configForInstance(bizProcess.getProcessInstanceId(), bizProcess.getModelId()).behavior();
        if (behavior != null && Boolean.FALSE.equals(behavior.getAllowWithdraw())) {
            throw new BusinessException("error.workflow.withdraw_not_allowed");
        }
    }

    @Override
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return Map.of();
        }
        ProcessInstance running = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeProcessVariables()
                .singleResult();
        if (running != null && running.getProcessVariables() != null) {
            return running.getProcessVariables();
        }
        HistoricProcessInstance historic = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeProcessVariables()
                .singleResult();
        if (historic != null && historic.getProcessVariables() != null) {
            return historic.getProcessVariables();
        }
        return Map.of();
    }

    /** Writes ownership for audit / future L3 lists; read path stays WorkflowAccessGuard. */
    private void applyOwnership(WfBizProcessEntity entity, String starterUserId) {
        String deptId = null;
        WorkflowOperatorContextResolver resolver = operatorContextResolver.getIfAvailable();
        if (resolver != null && StringUtils.hasText(starterUserId)) {
            WorkflowOperatorContextResolver.OperatorContext ctx = resolver.resolve(starterUserId);
            if (ctx != null) {
                deptId = ctx.deptId();
            }
        }
        OwnershipColumns.applyUserAndDept(entity::setUserId, entity::setDeptId, starterUserId, deptId);
    }

    private void assertStartAcl(WfModelEntity model, String starterUserId) {
        // 发起 ACL 以已发布版本快照为准（勿用设计师未发布草稿）
        WorkflowProcessConfigSupport.ProcessConfig config =
                versionConfigLookup.resolvePublishedByModelId(model.getId());
        String deptId = null;
        List<String> roleCodes = Collections.emptyList();
        List<String> postIds = Collections.emptyList();
        WorkflowOperatorContextResolver resolver = operatorContextResolver.getIfAvailable();
        if (resolver != null) {
            WorkflowOperatorContextResolver.OperatorContext ctx = resolver.resolve(starterUserId);
            if (ctx != null) {
                deptId = ctx.deptId();
                roleCodes = ctx.roleCodes() == null ? Collections.emptyList() : ctx.roleCodes();
                postIds = ctx.postIds() == null ? Collections.emptyList() : ctx.postIds();
            }
        }
        if (!processConfigSupport.isStartAllowed(config, starterUserId, deptId, roleCodes, postIds)) {
            throw new BusinessException("error.workflow.start_not_allowed");
        }
    }

    private WfModelEntity resolveModel(WorkflowStartRequest request) {
        // 运行 SoT：始终按 tenantId + bizType + binding policy 解析；modelId 仅一致性校验
        WfModelEntity model = workflowModelRegistry.requireActiveByBizType(
                request.getTenantId(), request.getBizType());
        if (StringUtils.hasText(request.getModelId())
                && !request.getModelId().trim().equals(model.getId())) {
            throw new BusinessException("error.workflow.model_id_mismatch");
        }
        if (model.getLatestVersion() == null || model.getLatestVersion() <= 0) {
            throw new BusinessException("error.workflow.model_not_deployed");
        }
        if (StringUtils.hasText(request.getProcessDefinitionKey())
                && !request.getProcessDefinitionKey().equals(model.getModelKey())) {
            throw new BusinessException("error.workflow.model_key_mismatch", model.getModelKey());
        }
        return model;
    }

    private Task requireAssignableTask(String taskId) {
        String userId = requireUserId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("error.workflow.task_not_found");
        }
        if (!userId.equals(task.getAssignee())) {
            throw new BusinessException("error.workflow.task_not_assignee");
        }
        return task;
    }

    @Override
    public void syncBizStatusIfEnded(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return;
        }
        ProcessInstance running = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (running != null) {
            return;
        }
        WfBizProcessEntity bizProcess = workflowBizProcessLookup.findByProcessInstanceId(processInstanceId);
        // 幂等：已终态不再派发业务回调 / 通知
        if (bizProcess != null) {
            String status = bizProcess.getStatus();
            if (WfBizProcessStatus.APPROVED.equals(status)
                    || WfBizProcessStatus.REJECTED.equals(status)
                    || WfBizProcessStatus.CANCELLED.equals(status)) {
                return;
            }
        }
        HistoricProcessInstance ended = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeProcessVariables()
                .singleResult();
        if (ended == null) {
            return;
        }
        Map<String, Object> variables = ended.getProcessVariables() == null
                ? Map.of() : ended.getProcessVariables();
        String endActivityId = ended.getEndActivityId();
        // 优先按结束节点判定：terminate endRejected 会带 deleteReason（TERMINATE_END_EVENT），不能当撤回跳过
        if (WorkflowEndEvents.APPROVED.equals(endActivityId)) {
            updateBizProcessStatus(processInstanceId, WfBizProcessStatus.APPROVED);
            dispatchApproved(variables, processInstanceId);
            publishLifecycleNotify(EVENT_COMPLETE, variables, processInstanceId, bizProcess,
                    List.of(), WorkflowProcessConfigSupport.Notify::isOnComplete);
            return;
        }
        if (WorkflowEndEvents.REJECTED.equals(endActivityId)) {
            updateBizProcessStatus(processInstanceId, WfBizProcessStatus.REJECTED);
            dispatchRejected(variables, processInstanceId);
            publishLifecycleNotify(EVENT_REJECT, variables, processInstanceId, bizProcess,
                    List.of(), WorkflowProcessConfigSupport.Notify::isOnReject);
            return;
        }
        // 撤回/删除结束：无标准结束节点，由 cancelProcess 写 cancelled，此处不覆盖
        if (StringUtils.hasText(ended.getDeleteReason())) {
            return;
        }
    }

    /** 当前活跃任务：抄送走 onCc，审批走 onTodo，收件人为任务办理人。 */
    private void notifyActiveAssignees(String processInstanceId,
                                       WfBizProcessEntity bizProcess,
                                       Map<String, Object> variables) {
        if (!StringUtils.hasText(processInstanceId) || bizProcess == null) {
            return;
        }
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        Set<String> todoRecipients = new LinkedHashSet<>();
        Set<String> ccRecipients = new LinkedHashSet<>();
        for (Task task : tasks) {
            if (task == null || !StringUtils.hasText(task.getAssignee())) {
                continue;
            }
            if (isCcTask(task)) {
                ccRecipients.add(task.getAssignee());
            } else {
                todoRecipients.add(task.getAssignee());
            }
        }
        if (!todoRecipients.isEmpty()) {
            publishLifecycleNotify(EVENT_TODO, variables, processInstanceId, bizProcess,
                    new ArrayList<>(todoRecipients), WorkflowProcessConfigSupport.Notify::isOnTodo);
        }
        if (!ccRecipients.isEmpty()) {
            publishLifecycleNotify(EVENT_CC, variables, processInstanceId, bizProcess,
                    new ArrayList<>(ccRecipients), WorkflowProcessConfigSupport.Notify::isOnCc);
        }
    }

    /** 抄送节点无驳回网关；设计器默认 key 以 cc 开头，BPMN category=cc。 */
    private boolean isCcTask(Task task) {
        return WorkflowCcTaskSupport.isCcTask(task);
    }

    private void publishLifecycleNotify(String eventType,
                                        Map<String, Object> variables,
                                        String processInstanceId,
                                        WfBizProcessEntity bizProcess,
                                        List<String> recipients,
                                        java.util.function.Predicate<WorkflowProcessConfigSupport.Notify> flag) {
        if (bizProcess == null || !StringUtils.hasText(bizProcess.getModelId())) {
            return;
        }
        WorkflowProcessConfigSupport.Notify notify =
                configForInstance(processInstanceId, bizProcess.getModelId()).notifyConfig();
        if (!flag.test(notify)) {
            return;
        }
        List<String> targetUsers = recipients;
        if (targetUsers == null || targetUsers.isEmpty()) {
            String starterUserId = bizProcess.getStarterUserId();
            if (!StringUtils.hasText(starterUserId)) {
                starterUserId = asString(variables.get(WorkflowVariables.STARTER_USER_ID));
            }
            if (!StringUtils.hasText(starterUserId)) {
                return;
            }
            targetUsers = List.of(starterUserId);
        }
        String bizTitle = StringUtils.hasText(bizProcess.getBizTitle())
                ? bizProcess.getBizTitle()
                : asString(variables.get(WorkflowVariables.BIZ_TITLE));
        String title = resolveNotifyTitle(eventType, bizTitle);
        String content = resolveNotifyContent(eventType, bizTitle, processInstanceId);
        WorkflowLifecycleNotifyEvent event = new WorkflowLifecycleNotifyEvent(
                bizProcess.getModelId(),
                processInstanceId,
                bizProcess.getBizId(),
                bizProcess.getBizType(),
                bizProcess.getTenantId(),
                eventType,
                targetUsers,
                title,
                content);
        notifyPublisher.publish(event);
    }

    /**
     * 标题契约见 {@link WorkflowTitleContract}：业务侧传入的 bizTitle 优先；
     * 缺省时用已发布版本的 titleTemplate（与发起 ACL 同源）。
     */
    private String resolveBizTitle(WorkflowStartRequest request, WfModelEntity model, LocalDateTime startTime) {
        if (StringUtils.hasText(request.getBizTitle())) {
            return WorkflowTitleContract.normalizeBizTitle(request.getBizTitle());
        }
        var behavior = versionConfigLookup.resolvePublishedByModelId(model.getId()).behavior();
        String template = behavior == null ? null : behavior.getTitleTemplate();
        if (!StringUtils.hasText(template) && behavior != null) {
            template = behavior.getSummaryTemplate();
        }
        String starter = SecurityContextHelper.getNickname();
        if (!StringUtils.hasText(starter)) {
            starter = SecurityContextHelper.getUsername();
        }
        if (!StringUtils.hasText(starter)) {
            starter = requireUserId();
        }
        String applied = WorkflowTitleTemplateSupport.apply(
                template,
                WorkflowTitleTemplateSupport.defaultTokens(
                        starter, model.getModelName(), request.getBizType(), startTime));
        return StringUtils.hasText(applied) ? applied : model.getModelName();
    }

    private WorkflowProcessConfigSupport.ProcessConfig configForInstance(String processInstanceId,
                                                                         String modelId) {
        return versionConfigLookup.resolveByProcessInstanceId(processInstanceId, modelId);
    }

    private String resolveNotifyTitle(String eventType, String bizTitle) {
        String eventLabel = i18nMessageResolver.resolve(notifyLabelKey(eventType));
        WorkflowNotifyTemplateSource.EventTemplate tpl = notifyTemplateSource.find(eventType).orElse(null);
        if (tpl != null && StringUtils.hasText(tpl.titlePattern())) {
            return applyNotifyPattern(tpl.titlePattern(), eventLabel, bizTitle, null, null);
        }
        if (StringUtils.hasText(bizTitle)) {
            return i18nMessageResolver.resolve("notify.workflow.title_with_biz", eventLabel, bizTitle);
        }
        return eventLabel;
    }

    private String resolveNotifyContent(String eventType, String bizTitle, String processInstanceId) {
        String title = resolveNotifyTitle(eventType, bizTitle);
        String eventLabel = i18nMessageResolver.resolve(notifyLabelKey(eventType));
        WorkflowNotifyTemplateSource.EventTemplate tpl = notifyTemplateSource.find(eventType).orElse(null);
        if (tpl != null && StringUtils.hasText(tpl.contentPattern())) {
            return applyNotifyPattern(tpl.contentPattern(), eventLabel, bizTitle, processInstanceId, title);
        }
        if (StringUtils.hasText(processInstanceId)) {
            return i18nMessageResolver.resolve("notify.workflow.content_with_instance", title, processInstanceId);
        }
        return title;
    }

    private static String applyNotifyPattern(String pattern, String eventLabel, String bizTitle,
                                             String processInstanceId, String title) {
        String out = pattern;
        out = out.replace("{eventLabel}", eventLabel == null ? "" : eventLabel);
        out = out.replace("{bizTitle}", bizTitle == null ? "" : bizTitle);
        out = out.replace("{instanceId}", processInstanceId == null ? "" : processInstanceId);
        out = out.replace("{title}", title == null ? "" : title);
        return out.trim();
    }

    private static String notifyLabelKey(String eventType) {
        return switch (eventType) {
            case EVENT_REJECT -> "notify.workflow.reject";
            case EVENT_COMPLETE -> "notify.workflow.complete";
            case EVENT_RETURN -> "notify.workflow.return";
            case EVENT_TODO -> "notify.workflow.todo";
            case EVENT_CC -> "notify.workflow.cc";
            case EVENT_URGE -> "notify.workflow.urge";
            case "TIMEOUT" -> "notify.workflow.timeout";
            default -> "notify.workflow.default";
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void linkSubprocessInstance(String childProcessInstanceId, String parentProcessInstanceId) {
        if (!StringUtils.hasText(childProcessInstanceId) || !StringUtils.hasText(parentProcessInstanceId)) {
            return;
        }
        if (childProcessInstanceId.equals(parentProcessInstanceId)) {
            return;
        }
        WfBizProcessEntity parent = wfBizProcessMapper.selectByProcessInstanceId(parentProcessInstanceId);
        if (parent == null) {
            return;
        }
        WfBizProcessEntity child = wfBizProcessMapper.selectByProcessInstanceId(childProcessInstanceId);
        if (child != null) {
            if (!StringUtils.hasText(child.getParentInstanceId())) {
                wfBizProcessMapper.updateParentInstanceId(childProcessInstanceId, parentProcessInstanceId);
            }
            return;
        }
        ProcessInstance childPi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(childProcessInstanceId)
                .singleResult();
        WfBizProcessEntity row = new WfBizProcessEntity();
        row.setId(IdGeneratorUtils.nextIdStr());
        row.setTenantId(parent.getTenantId());
        row.setModelId(parent.getModelId());
        row.setModelVersionNo(parent.getModelVersionNo());
        row.setBizType(parent.getBizType());
        row.setBizId(parent.getBizId());
        row.setBizTitle(parent.getBizTitle());
        row.setProcessDefinitionKey(childPi != null && StringUtils.hasText(childPi.getProcessDefinitionKey())
                ? childPi.getProcessDefinitionKey()
                : parent.getProcessDefinitionKey());
        row.setProcessInstanceId(childProcessInstanceId);
        row.setParentInstanceId(parentProcessInstanceId);
        row.setStatus(WfBizProcessStatus.RUNNING);
        row.setStarterUserId(parent.getStarterUserId());
        applyOwnership(row, parent.getStarterUserId());
        if (!StringUtils.hasText(row.getDeptId()) && StringUtils.hasText(parent.getDeptId())) {
            row.setDeptId(parent.getDeptId());
        }
        row.setStartTime(LocalDateTime.now());
        row.setRemark("subprocess");
        AuditFieldHelper.fillInsert(row);
        wfBizProcessMapper.insert(row);
    }

    private void updateBizProcessStatus(String processInstanceId, String status) {
        LocalDateTime endTime = null;
        if (WfBizProcessStatus.APPROVED.equals(status)
                || WfBizProcessStatus.REJECTED.equals(status)
                || WfBizProcessStatus.CANCELLED.equals(status)) {
            endTime = LocalDateTime.now();
        }
        wfBizProcessMapper.updateStatusByProcessInstanceId(processInstanceId, status, endTime);
    }

    private void dispatchApproved(Map<String, Object> variables, String processInstanceId) {
        dispatch(variables, processInstanceId, WorkflowBizHandler::onProcessApproved);
    }

    private void dispatchRejected(Map<String, Object> variables, String processInstanceId) {
        dispatch(variables, processInstanceId, WorkflowBizHandler::onProcessRejected);
    }

    private void dispatchCancelled(Map<String, Object> variables, String processInstanceId) {
        dispatch(variables, processInstanceId, WorkflowBizHandler::onProcessCancelled);
    }

    private void dispatchReturned(Map<String, Object> variables, String processInstanceId, String rejectMode) {
        String bizType = asString(variables.get(WorkflowVariables.BIZ_TYPE));
        String bizId = asString(variables.get(WorkflowVariables.BIZ_ID));
        if (!StringUtils.hasText(bizType) || !StringUtils.hasText(bizId)) {
            return;
        }
        for (WorkflowBizHandler handler : bizHandlers) {
            if (handler.supports(bizType)) {
                handler.onProcessReturned(bizId, processInstanceId, rejectMode);
                return;
            }
        }
    }

    private void applyApproveFormPatch(Task task, WfBizProcessEntity biz, Map<String, Object> formData) {
        if (biz == null || !StringUtils.hasText(biz.getBizType()) || !StringUtils.hasText(biz.getBizId())) {
            return;
        }
        WorkflowModelDesignNodeRequest node = designNodeLookup.findByProcessInstanceAndTaskKey(
                task.getProcessInstanceId(), task.getTaskDefinitionKey());
        Map<String, Object> writable = WorkflowNodeFieldPermSupport.filterWritableOnly(node, formData);
        if (writable.isEmpty()) {
            return;
        }
        for (WorkflowApproveFormHandler handler : approveFormHandlers) {
            if (handler.supports(biz.getBizType())) {
                handler.applyWritablePatch(
                        biz.getBizId(),
                        task.getProcessInstanceId(),
                        task.getTaskDefinitionKey(),
                        writable);
                return;
            }
        }
    }

    private void enrichRuntimeVariables(WfBizProcessEntity biz, String processInstanceId) {
        if (biz == null || !StringUtils.hasText(biz.getBizType()) || !StringUtils.hasText(biz.getBizId())
                || !StringUtils.hasText(processInstanceId)) {
            return;
        }
        for (WorkflowRuntimeVariableEnricher enricher : runtimeVariableEnrichers) {
            if (!enricher.supports(biz.getBizType())) {
                continue;
            }
            Map<String, Object> vars = enricher.enrich(biz.getBizType(), biz.getBizId(), processInstanceId);
            if (vars == null || vars.isEmpty()) {
                continue;
            }
            runtimeService.setVariables(processInstanceId, vars);
        }
    }

    private void dispatch(Map<String, Object> variables,
                          String processInstanceId,
                          BizCallback callback) {
        String bizType = asString(variables.get(WorkflowVariables.BIZ_TYPE));
        String bizId = asString(variables.get(WorkflowVariables.BIZ_ID));
        if (!StringUtils.hasText(bizType) || !StringUtils.hasText(bizId)) {
            return;
        }
        for (WorkflowBizHandler handler : bizHandlers) {
            if (handler.supports(bizType)) {
                callback.invoke(handler, bizId, processInstanceId);
                return;
            }
        }
    }

    private String requireUserId() {
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("error.unauthorized");
        }
        return userId;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @FunctionalInterface
    private interface BizCallback {
        void invoke(WorkflowBizHandler handler, String bizId, String processInstanceId);
    }
}
