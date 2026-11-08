package com.icoolkj.platform.system.notice.constant;

/**
 * 公告调用工作流 cancel 时传入的原因码（写入 wf_task_action，前端按码 i18n）。
 * <p>勿放入 workflow 模块常量，避免工作流依赖业务词表。
 */
public final class NoticeWorkflowCancelReasons {

    public static final String WITHDRAW = "notice_withdraw";
    public static final String CLOSE_AFTER_RETURN = "notice_close_after_return";

    private NoticeWorkflowCancelReasons() {
    }
}
