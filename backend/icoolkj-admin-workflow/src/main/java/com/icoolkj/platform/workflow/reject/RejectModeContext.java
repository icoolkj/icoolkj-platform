package com.icoolkj.platform.workflow.reject;

import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;

/** 驳回跳转所需引擎与业务协作能力（由 ProcessService 注入实现）。 */
public interface RejectModeContext {

    RuntimeService runtimeService();

    TaskService taskService();

    /** 同节点/跨节点跳转后写审计、通知与业务 returned 回调 */
    void afterRejectJump(Task task, WfBizProcessEntity biz, String rejectMode, String targetKey, String comment);
}
