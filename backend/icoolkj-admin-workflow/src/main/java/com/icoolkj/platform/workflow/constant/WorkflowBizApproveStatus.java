package com.icoolkj.platform.workflow.constant;

/**
 * 独立业务表审批状态码（与 OA {@code approve_status} 对齐）。
 * <p>业务结果（如公告 {@code published_at}）另开列，勿用业务结果码顶替本状态机。
 */
public final class WorkflowBizApproveStatus {

    public static final String DRAFT = "draft";
    public static final String RUNNING = "running";
    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";
    public static final String CANCELLED = "cancelled";
    /** 审批人 starterEdit 退回：发起人可改单后重提（流程挂起） */
    public static final String RETURNED = "returned";
    /** 归档；保留单据与审批历史，不可编辑/删除 */
    public static final String CLOSED = "closed";

    private WorkflowBizApproveStatus() {
    }

    /** 可编辑并再发起（不含 returned 的「改后重提」语义，returned 单独判断） */
    public static boolean isResubmittable(String status) {
        return DRAFT.equals(status) || REJECTED.equals(status) || CANCELLED.equals(status);
    }

    public static boolean isEditable(String status) {
        return isResubmittable(status) || RETURNED.equals(status);
    }
}
