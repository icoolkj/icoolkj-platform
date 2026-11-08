package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.service.WorkflowProcessService;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.delegate.event.FlowableProcessStartedEvent;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * callActivity 子流程启动时写入 {@code wf_biz_process.parent_instance_id} 并补桥接行。
 */
@Component
public class WorkflowSubprocessStartedListener implements FlowableEventListener {

    private static final Logger log = LoggerFactory.getLogger(WorkflowSubprocessStartedListener.class);

    private final WorkflowProcessService workflowProcessService;

    public WorkflowSubprocessStartedListener(@Lazy WorkflowProcessService workflowProcessService) {
        this.workflowProcessService = workflowProcessService;
    }

    @Override
    public void onEvent(FlowableEvent event) {
        if (event == null || event.getType() != FlowableEngineEventType.PROCESS_STARTED) {
            return;
        }
        if (!(event instanceof FlowableProcessStartedEvent started)) {
            return;
        }
        Object entity = started.getEntity();
        if (!(entity instanceof ExecutionEntity execution)) {
            return;
        }
        if (!StringUtils.hasText(execution.getSuperExecutionId())) {
            return;
        }
        ExecutionEntity superExecution = execution.getSuperExecution();
        if (superExecution == null) {
            return;
        }
        String childPi = execution.getProcessInstanceId();
        String parentPi = superExecution.getProcessInstanceId();
        if (!StringUtils.hasText(childPi) || !StringUtils.hasText(parentPi) || childPi.equals(parentPi)) {
            return;
        }
        try {
            workflowProcessService.linkSubprocessInstance(childPi, parentPi);
        } catch (Exception ex) {
            log.error("Failed to link subprocess wf_biz_process, child={} parent={}", childPi, parentPi, ex);
        }
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
