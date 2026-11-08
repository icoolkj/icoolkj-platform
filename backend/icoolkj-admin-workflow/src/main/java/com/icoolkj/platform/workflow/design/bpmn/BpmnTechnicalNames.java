package com.icoolkj.platform.workflow.design.bpmn;

/**
 * BPMN {@code name} 属性的语言中性技术文案（仅引擎/调试可见）。
 * <p>元素 {@code id} 走 {@link BpmnElementIds}；产品 UI 展示走 design JSON。
 */
public final class BpmnTechnicalNames {

    public static final String START = "start";
    public static final String END_APPROVED = "endApproved";
    public static final String END_REJECTED = "endRejected";
    public static final String MERGE = "merge";
    public static final String INCLUSIVE_JOIN = "inclusiveJoin";
    public static final String PARALLEL_JOIN = "parallelJoin";
    public static final String BRANCH_YES = "branchYes";
    public static final String BRANCH_DEFAULT = "branchDefault";

    private BpmnTechnicalNames() {
    }

    /** 审批结果网关显示名 */
    public static String resultGateway(String displayName) {
        return displayName + "Result";
    }

    public static String parallelBranch(int index) {
        return "parallel" + index;
    }
}
