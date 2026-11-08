package com.icoolkj.platform.workflow.event;

/** 发布流程生命周期通知（可由 ApplicationEventPublisher 适配） */
@FunctionalInterface
public interface WorkflowNotifyPublisher {

    void publish(WorkflowLifecycleNotifyEvent event);
}
