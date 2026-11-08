package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.constant.WfTaskActionTypes;
import com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import com.icoolkj.platform.workflow.event.WorkflowLifecycleNotifyEvent;
import com.icoolkj.platform.workflow.event.WorkflowNotifyPublisher;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import com.icoolkj.platform.workflow.spi.WorkflowNotifyTemplateSource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * boundary timer end：写审计 + TIMEOUT 通知（受版本快照 notify.onTimeout）。
 * 非中断边界仅通知；中断 rejectEnd 连 endRejected；中断 autoApprove 由
 * {@link WorkflowTimeoutAutoApproveListener} 写 approved 后进下一节点。
 */
@Component(BpmnElementIds.BEAN_TIMEOUT_LISTENER)
public class WorkflowTimeoutListener implements ExecutionListener {

    public static final String EVENT_TIMEOUT = "TIMEOUT";

    private final WorkflowBizProcessLookup bizProcessLookup;
    private final WorkflowVersionConfigLookup versionConfigLookup;
    private final WorkflowTaskActionRecorder actionRecorder;
    private final TaskService taskService;
    private final WorkflowNotifyPublisher notifyPublisher;
    private final I18nMessageResolver i18nMessageResolver;
    private final WorkflowNotifyTemplateSource notifyTemplateSource;

    public WorkflowTimeoutListener(WorkflowBizProcessLookup bizProcessLookup,
                                   WorkflowVersionConfigLookup versionConfigLookup,
                                   WorkflowTaskActionRecorder actionRecorder,
                                   TaskService taskService,
                                   ObjectProvider<WorkflowNotifyPublisher> notifyPublisherProvider,
                                   ApplicationEventPublisher eventPublisher,
                                   I18nMessageResolver i18nMessageResolver,
                                   WorkflowNotifyTemplateSource notifyTemplateSource) {
        this.bizProcessLookup = bizProcessLookup;
        this.versionConfigLookup = versionConfigLookup;
        this.actionRecorder = actionRecorder;
        this.taskService = taskService;
        WorkflowNotifyPublisher custom = notifyPublisherProvider.getIfAvailable();
        this.notifyPublisher = custom != null ? custom : eventPublisher::publishEvent;
        this.i18nMessageResolver = i18nMessageResolver;
        this.notifyTemplateSource = notifyTemplateSource;
    }

    @Override
    public void notify(DelegateExecution execution) {
        String pi = execution.getProcessInstanceId();
        String activityId = execution.getCurrentActivityId();
        String taskKey = activityId;
        if (StringUtils.hasText(activityId) && activityId.startsWith("boundary_") && activityId.endsWith("_timeout")) {
            taskKey = activityId.substring("boundary_".length(), activityId.length() - "_timeout".length());
        }
        WfBizProcessEntity biz = bizProcessLookup.findByProcessInstanceId(pi);
        actionRecorder.record(
                biz == null ? null : biz.getTenantId(),
                pi,
                null,
                taskKey,
                WfTaskActionTypes.TIMEOUT,
                null,
                null,
                null,
                null);

        if (biz == null || !StringUtils.hasText(biz.getModelId())) {
            return;
        }
        var notify = versionConfigLookup.resolveByProcessInstanceId(pi, biz.getModelId()).notifyConfig();
        if (!notify.isOnTimeout()) {
            return;
        }
        Set<String> recipients = new LinkedHashSet<>();
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi).list();
        if (tasks != null) {
            for (Task t : tasks) {
                if (t != null && StringUtils.hasText(t.getAssignee()) && !WorkflowCcTaskSupport.isCcTask(t)) {
                    recipients.add(t.getAssignee());
                }
            }
        }
        if (recipients.isEmpty() && StringUtils.hasText(biz.getStarterUserId())) {
            recipients.add(biz.getStarterUserId());
        }
        if (recipients.isEmpty()) {
            return;
        }
        String bizTitle = biz.getBizTitle();
        String eventLabel = i18nMessageResolver.resolve("notify.workflow.timeout");
        String title = i18nMessageResolver.resolve("notify.workflow.title_with_biz", eventLabel, bizTitle);
        String content = i18nMessageResolver.resolve("notify.workflow.content_with_instance", title, pi);
        var tpl = notifyTemplateSource.find(EVENT_TIMEOUT).orElse(null);
        if (tpl != null && StringUtils.hasText(tpl.titlePattern())) {
            title = tpl.titlePattern()
                    .replace("{eventLabel}", eventLabel)
                    .replace("{bizTitle}", bizTitle == null ? "" : bizTitle)
                    .replace("{instanceId}", pi);
        }
        if (tpl != null && StringUtils.hasText(tpl.contentPattern())) {
            content = tpl.contentPattern()
                    .replace("{eventLabel}", eventLabel)
                    .replace("{bizTitle}", bizTitle == null ? "" : bizTitle)
                    .replace("{instanceId}", pi)
                    .replace("{title}", title);
        }
        notifyPublisher.publish(new WorkflowLifecycleNotifyEvent(
                biz.getModelId(),
                pi,
                biz.getBizId(),
                biz.getBizType(),
                biz.getTenantId(),
                EVENT_TIMEOUT,
                new ArrayList<>(recipients),
                title,
                content));
    }
}
