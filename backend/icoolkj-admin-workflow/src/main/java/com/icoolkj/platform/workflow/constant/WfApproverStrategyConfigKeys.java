package com.icoolkj.platform.workflow.constant;

/** 审批人 strategyConfig JSON 字段契约 */
public final class WfApproverStrategyConfigKeys {

    public static final String USER_ID = "userId";
    public static final String ROLE_CODES = "roleCodes";
    public static final String EXPR = "expr";

    /** expr=starter → 发起人 */
    public static final String EXPR_STARTER = "starter";
    /** expr=variable:&lt;name&gt; → 读流程变量中的用户 id */
    public static final String EXPR_VARIABLE_PREFIX = "variable:";
    /** expr=userId:&lt;id&gt; → 字面量用户 id（服务账号等） */
    public static final String EXPR_USER_ID_PREFIX = "userId:";

    private WfApproverStrategyConfigKeys() {
    }
}
