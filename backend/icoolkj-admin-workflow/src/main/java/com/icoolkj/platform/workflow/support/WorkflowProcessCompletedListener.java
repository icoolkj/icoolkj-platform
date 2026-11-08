package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.service.WorkflowProcessService;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 流程结束（含超时/驳回 terminate 直达 endRejected）时同步 {@code wf_biz_process}。
 * 与 {@code completeTask} 内同步幂等，避免超时路径漏写业务状态。
 */
@Component
public class WorkflowProcessCompletedListener implements FlowableEventListener {

    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessCompletedListener.class);

    private final WorkflowProcessService workflowProcessService;

    public WorkflowProcessCompletedListener(@Lazy WorkflowProcessService workflowProcessService) {
        this.workflowProcessService = workflowProcessService;
    }

    @Override
    public void onEvent(FlowableEvent event) {
        if (event == null) {
            return;
        }
        // 普通 endApproved → PROCESS_COMPLETED；terminate endRejected → PROCESS_COMPLETED_WITH_TERMINATE_END_EVENT
        if (event.getType() != FlowableEngineEventType.PROCESS_COMPLETED
                && event.getType() != FlowableEngineEventType.PROCESS_COMPLETED_WITH_TERMINATE_END_EVENT) {
            return;
        }
        String processInstanceId = resolveProcessInstanceId(event);
        if (!StringUtils.hasText(processInstanceId)) {
            return;
        }
        try {
            workflowProcessService.syncBizStatusIfEnded(processInstanceId);
        } catch (Exception ex) {
            log.error("Failed to sync wf_biz_process after process end, pi={}", processInstanceId, ex);
        }
    }

    private static String resolveProcessInstanceId(FlowableEvent event) {
        if (event instanceof FlowableEngineEntityEvent entityEvent) {
            Object entity = entityEvent.getEntity();
            if (entity instanceof ExecutionEntity execution) {
                if (StringUtils.hasText(execution.getProcessInstanceId())) {
                    return execution.getProcessInstanceId();
                }
                return execution.getId();
            }
        }
        return null;
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
