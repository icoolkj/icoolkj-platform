package com.icoolkj.platform.system.ai.constant;

/**
 * Platform AI skill codes and categories (bound to {@code sys_ai_skill.skill_code}).
 */
public final class AiSkillConstants {

    public static final String CATEGORY_OFFICE = "OFFICE";
    public static final String CATEGORY_TOOL = "TOOL";
    public static final String CATEGORY_OPS = "OPS";

    /** 查待办 */
    public static final String CODE_LIST_MY_TODOS = "list_my_todos";
    /** 查我发起的 */
    public static final String CODE_LIST_MY_SUBMITTED = "list_my_submitted";

    private AiSkillConstants() {
    }
}
