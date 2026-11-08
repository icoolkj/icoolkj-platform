package com.icoolkj.platform.workflow.action;

/**
 * 运行时任务动作扩展点（Strategy）。
 * <p>当前 transfer / delegate / urge / addSign / removeSign / jump 仍由
 * {@code WorkflowProcessServiceImpl} 编排；新增动作优先实现本接口并注册到
 * {@link WorkflowTaskActionHandlerRegistry}，再由 Facade 委派，避免继续堆 if。
 */
public interface WorkflowTaskActionHandler {

    /** 与 {@link com.icoolkj.platform.workflow.constant.WfTaskActionTypes} 对齐 */
    String actionType();
}
