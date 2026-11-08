package com.icoolkj.platform.workflow.spi;

/**
 * 流程生命周期回调：终态 + 非终态退回跳转。
 * <p><b>边界（强制）</b>：实现类只同步「审批字段包」——
 * {@code approve_status} / {@code process_instance_id} / {@code finished_at}（及文档约定的 returned 映射）。
 * 禁止在此写入业务结果字段（如 {@code release_status}、门户上架、行启停 {@code status}、附件清理等）；
 * 那些逻辑留在业务 Service（close / publish / delete）。
 */
public interface WorkflowBizHandler {

    boolean supports(String bizType);

    void onProcessApproved(String bizId, String processInstanceId);

    void onProcessRejected(String bizId, String processInstanceId);

    void onProcessCancelled(String bizId, String processInstanceId);

    /**
     * 驳回跳转（rejectMode=previous/starter/starterEdit）：流程未结束，业务侧可感知。
     * 默认空实现，避免破坏既有 Handler。
     */
    default void onProcessReturned(String bizId, String processInstanceId, String rejectMode) {
    }

    /**
     * 业务单据当前审批/归档状态（draft/running/…/closed），供已发申请等列表只读展示。
     * 无业务表或不支持时返回 null。
     */
    default String resolveApproveStatus(String bizId) {
        return null;
    }
}
