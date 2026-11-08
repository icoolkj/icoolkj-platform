package com.icoolkj.platform.workflow.constant;

/** wf_task_action.action_type */
public final class WfTaskActionTypes {

    public static final String TRANSFER = "transfer";
    public static final String DELEGATE = "delegate";
    public static final String URGE = "urge";
    public static final String REJECT_JUMP = "reject_jump";
    /** 管理员自由跳转（非驳回） */
    public static final String JUMP = "jump";
    /** 发起人撤回 / 取消实例 */
    public static final String WITHDRAW = "withdraw";
    /** 管理员在流程监控终止实例（与发起人 withdraw 区分） */
    public static final String TERMINATE = "terminate";
    public static final String TIMEOUT = "timeout";
    public static final String ADD_SIGN_BEFORE = "add_sign_before";
    public static final String ADD_SIGN_AFTER = "add_sign_after";
    public static final String REMOVE_SIGN = "remove_sign";

    /** payload_json.phase：后加签交接（当前人通过后转给加签人） */
    public static final String PHASE_HANDOFF = "handoff";
    /** payload_json.phase：前加签归还（加签人通过后交回原办理人） */
    public static final String PHASE_RETURN = "return";

    private WfTaskActionTypes() {
    }
}
