package com.icoolkj.platform.workflow.design.bpmn;

import com.icoolkj.platform.workflow.constant.WorkflowEndEvents;
import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import org.springframework.util.StringUtils;

/**
 * 平台 BPMN 元素 ID / 连线 / 变量 / Listener / EL 唯一规约。
 * <p>Emitter 禁止再拼前缀字符串；运行时读变量也必须走本类，保证编译与执行同一套约定。
 * <p>产品 UI 展示名仍走 design JSON，与本类无关（见 {@link BpmnTechnicalNames}）。
 */
public final class BpmnElementIds {

    /** 固定骨架：起止事件、起始线 */
    public static final String START = "startEvent";
    public static final String END_APPROVED = WorkflowEndEvents.APPROVED;
    public static final String END_REJECTED = WorkflowEndEvents.REJECTED;
    public static final String FLOW_START = "flowStart";

    /** Spring Bean 名（委托表达式不含 ${}） */
    public static final String BEAN_ASSIGNEE_TASK_LISTENER = "workflowAssigneeTaskListener";
    public static final String BEAN_ASSIGNEE_EXECUTION_LISTENER = "workflowAssigneeExecutionListener";
    public static final String BEAN_TIMEOUT_LISTENER = "workflowTimeoutListener";
    /** 超时自动通过：写入 approved=true */
    public static final String BEAN_TIMEOUT_AUTO_APPROVE_LISTENER = "workflowTimeoutAutoApproveListener";

    private BpmnElementIds() {
    }

    // ─── approve：结果网关 + 通过/驳回线 ───

    /** 审批结果排他网关：{@code gw_{taskKey}} */
    public static String resultGatewayId(String taskKey) {
        return "gw_" + taskKey;
    }

    /** userTask → 结果网关 */
    public static String flowToResultGateway(String taskKey) {
        return "flowTask_" + taskKey;
    }

    /** 结果网关 → 下一节点（通过） */
    public static String flowApprove(String taskKey) {
        return "flowApprove_" + taskKey;
    }

    /** 结果网关 → endRejected（默认流） */
    public static String flowReject(String taskKey) {
        return "flowReject_" + taskKey;
    }

    /** 抄送等无网关节点：task → 下一目标 */
    public static String flowFromTask(String taskKey) {
        return "flowTask_" + taskKey;
    }

    // ─── condition：分裂 / 汇合 ───

    public static String conditionSplitId(String taskKey) {
        return "cgw_" + taskKey;
    }

    public static String conditionMergeId(String taskKey) {
        return "mgw_" + taskKey;
    }

    public static String conditionDefaultFlowId(String taskKey) {
        return "flowCondDef_" + taskKey;
    }

    public static String conditionBranchFlowId(String taskKey, String branchId) {
        return "flowCond_" + taskKey + "_" + safeToken(branchId, "yes");
    }

    public static String conditionMergeOutFlowId(String taskKey) {
        return "flowMerge_" + taskKey;
    }

    // ─── inclusive：分裂 / 汇合 ───

    public static String inclusiveSplitId(String taskKey) {
        return "igw_" + taskKey;
    }

    public static String inclusiveMergeId(String taskKey) {
        return "igw_join_" + taskKey;
    }

    public static String inclusiveDefaultFlowId(String taskKey) {
        return "flowIncDef_" + taskKey;
    }

    public static String inclusiveBranchFlowId(String taskKey, String branchId) {
        return "flowInc_" + taskKey + "_" + safeToken(branchId, "yes");
    }

    public static String inclusiveMergeOutFlowId(String taskKey) {
        return "flowIncJoin_" + taskKey;
    }

    // ─── parallel：fork / join ───

    public static String parallelForkId(String taskKey) {
        return "pgw_fork_" + taskKey;
    }

    public static String parallelJoinId(String taskKey) {
        return "pgw_join_" + taskKey;
    }

    public static String parallelBranchFlowId(String taskKey, int index) {
        return "flowPar_" + taskKey + "_" + index;
    }

    public static String parallelJoinOutFlowId(String taskKey) {
        return "flowParJoin_" + taskKey;
    }

    // ─── 流程变量约定 ───

    /** 会签/或签办理人列表：{@code {taskKey}AssigneeList} */
    public static String assigneeListVar(String taskKey) {
        return taskKey + "AssigneeList";
    }

    // ─── EL / 委托表达式 ───

    public static String el(String expressionBody) {
        return "${" + expressionBody + "}";
    }

    public static String varEl(String variableName) {
        return el(variableName);
    }

    public static String approvedConditionEl() {
        return varEl(WorkflowVariables.APPROVED);
    }

    /** 或签：任一完成即结束多实例 */
    public static String multiInstancePassAnyEl() {
        return el("nrOfCompletedInstances >= 1");
    }

    /** 会签：驳回立即结束，或全部完成 */
    public static String multiInstancePassAllEl() {
        return el(WorkflowVariables.APPROVED + " == false || nrOfCompletedInstances == nrOfInstances");
    }

    public static String assigneeTaskListenerEl() {
        return varEl(BEAN_ASSIGNEE_TASK_LISTENER);
    }

    public static String assigneeExecutionListenerEl() {
        return varEl(BEAN_ASSIGNEE_EXECUTION_LISTENER);
    }

    public static String timeoutListenerEl() {
        return varEl(BEAN_TIMEOUT_LISTENER);
    }

    public static String timeoutAutoApproveListenerEl() {
        return varEl(BEAN_TIMEOUT_AUTO_APPROVE_LISTENER);
    }

    /** 边界定时器：boundary_{taskKey}_timeout */
    public static String timeoutBoundaryId(String taskKey) {
        return "boundary_" + taskKey + "_timeout";
    }

    /** 延时节点 intermediateCatchEvent：delay_{taskKey} */
    public static String delayEventId(String taskKey) {
        return "delay_" + taskKey;
    }

    /** delay 节点出口连线 */
    public static String flowFromDelay(String taskKey) {
        return "flowDelay_" + taskKey;
    }

    public static String timeoutBoundaryFlowId(String taskKey) {
        return "flowTimeout_" + taskKey;
    }

    /** 非中断超时通知汇点（无业务副作用） */
    public static String timeoutNotifySinkId(String taskKey) {
        return "timeoutSink_" + taskKey;
    }

    private static String safeToken(String raw, String fallback) {
        String safe = StringUtils.hasText(raw)
                ? raw.trim().replaceAll("[^a-zA-Z0-9_]", "_")
                : fallback;
        return safe.isEmpty() ? fallback : safe;
    }
}
