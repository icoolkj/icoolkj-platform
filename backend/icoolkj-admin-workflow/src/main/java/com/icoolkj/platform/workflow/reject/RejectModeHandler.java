package com.icoolkj.platform.workflow.reject;

import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import org.flowable.task.api.Task;

/**
 * 驳回流转策略（Strategy）。新增 rejectMode 时新增 Handler Bean，禁止在 complete 路径堆 if。
 * <p>{@code end} 无 Handler：走 BPMN 默认驳回结束。
 */
public interface RejectModeHandler {

    /** 与 design.rejectMode 对齐：previous / starter / starterEdit */
    String rejectMode();

    /**
     * @return true 表示已消费本次驳回（已跳转/挂起），调用方勿再 complete→endRejected
     */
    boolean handle(Task task, WfBizProcessEntity biz, String comment, RejectModeContext context);
}
