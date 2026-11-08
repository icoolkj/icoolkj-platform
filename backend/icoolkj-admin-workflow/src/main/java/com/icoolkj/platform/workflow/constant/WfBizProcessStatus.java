package com.icoolkj.platform.workflow.constant;

/** wf_biz_process.status */
public final class WfBizProcessStatus {

    public static final String RUNNING = "running";
    /** 退回发起人修改中（starterEdit 挂起） */
    public static final String RETURNED = "returned";
    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";
    public static final String CANCELLED = "cancelled";

    private WfBizProcessStatus() {
    }
}
