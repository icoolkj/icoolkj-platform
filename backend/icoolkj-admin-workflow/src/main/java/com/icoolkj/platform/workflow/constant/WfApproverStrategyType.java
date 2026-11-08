package com.icoolkj.platform.workflow.constant;

/** 审批人解析策略类型 */
public final class WfApproverStrategyType {

    public static final String ROLE = "role";
    public static final String FIXED_USER = "fixed_user";
    /** 发起人所在部门的指定角色 */
    public static final String STARTER_DEPT_ROLE = "starter_dept_role";
    /** 简单表达式（Phase 1 仅支持 starter） */
    public static final String EXPRESSION = "expression";

    private WfApproverStrategyType() {
    }
}
