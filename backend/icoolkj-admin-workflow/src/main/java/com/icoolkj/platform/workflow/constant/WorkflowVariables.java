package com.icoolkj.platform.workflow.constant;

import java.util.Set;

/** Flowable 流程变量 key */
public final class WorkflowVariables {

    public static final String BIZ_TYPE = "bizType";
    public static final String BIZ_ID = "bizId";
    public static final String BIZ_TITLE = "bizTitle";
    public static final String TENANT_ID = "tenantId";
    public static final String STARTER_USER_ID = "starterUserId";
    public static final String STARTER_USER_NAME = "starterUserName";
    public static final String APPROVED = "approved";
    /** 流程模型 id，运行时按节点解析审批人用 */
    public static final String MODEL_ID = "modelId";
    /** 上一审批 userTask id（驳回 previous） */
    public static final String LAST_USER_TASK_KEY = "wfLastUserTaskKey";
    /** 首个审批 userTask id（驳回 starter） */
    public static final String FIRST_USER_TASK_KEY = "wfFirstUserTaskKey";
    /** 前加签：原办理人，加签人通过后归还 */
    public static final String ADD_SIGN_RETURN_USER_ID = "wfAddSignReturnUserId";
    /** 后加签：待注入的办理人列表 JSON/逗号分隔由服务写入 */
    public static final String ADD_SIGN_AFTER_USER_IDS = "wfAddSignAfterUserIds";
    /** starterEdit：流程已挂起，待发起人改单后重提 */
    public static final String STARTER_EDIT_PENDING = "wfStarterEditPending";
    /** 委派：原办理人 userId，任务 local 变量，完成时无需归还 */
    public static final String DELEGATE_OWNER_USER_ID = "wfDelegateOwnerUserId";
    /** 委派截止时刻（ISO-8601 字符串），任务 local 变量，过期后禁止办理 */
    public static final String DELEGATE_UNTIL = "wfDelegateUntil";

    private static final Set<String> RESERVED = Set.of(
            BIZ_TYPE,
            BIZ_ID,
            BIZ_TITLE,
            TENANT_ID,
            STARTER_USER_ID,
            STARTER_USER_NAME,
            APPROVED,
            MODEL_ID,
            LAST_USER_TASK_KEY,
            FIRST_USER_TASK_KEY,
            ADD_SIGN_RETURN_USER_ID,
            ADD_SIGN_AFTER_USER_IDS,
            STARTER_EDIT_PENDING,
            DELEGATE_OWNER_USER_ID,
            DELEGATE_UNTIL
    );

    private WorkflowVariables() {
    }

    /** 系统保留键：请求 variables / SPI 不得覆盖（契约 27） */
    public static Set<String> reservedKeys() {
        return RESERVED;
    }

    public static boolean isReserved(String key) {
        return key != null && RESERVED.contains(key);
    }
}
