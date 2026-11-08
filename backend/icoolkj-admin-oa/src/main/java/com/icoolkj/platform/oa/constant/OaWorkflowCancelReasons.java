package com.icoolkj.platform.oa.constant;

/**
 * OA 调用工作流 cancel 时传入的原因码（写入 wf_task_action，前端按码 i18n）。
 * <p>勿放入 workflow 模块常量，避免工作流依赖业务词表。
 */
public final class OaWorkflowCancelReasons {

    /** 历史记录展示用；运行中撤回已改为已发 {@code starter_withdraw} */
    public static final String WITHDRAW = "oa_instance_withdraw";

    public static final String CLOSE_AFTER_RETURN = "oa_close_after_return";

    private OaWorkflowCancelReasons() {
    }
}
