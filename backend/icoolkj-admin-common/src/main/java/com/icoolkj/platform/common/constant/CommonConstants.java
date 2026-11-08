package com.icoolkj.platform.common.constant;

/**
 * Platform-wide constants.
 */
public final class CommonConstants {

    /**
     * Fallback operator id when no authenticated user is present.
     */
    public static final String SYSTEM_USER_ID = "0";

    /** DB char(1) 是否标志：是。 */
    public static final String YES = "1";

    /** DB char(1) 是否标志：否。 */
    public static final String NO = "0";

    /**
     * 通用业务状态：正常 / 启用（与字典 sys_normal_disable 一致）。
     */
    public static final String STATUS_NORMAL = "0";

    /**
     * 通用业务状态：停用。
     */
    public static final String STATUS_DISABLE = "1";

    /**
     * 树形结构根节点 parentId（菜单、部门等）。
     */
    public static final String TREE_ROOT_PARENT_ID = "0";

    /** 性别：男（字典 sys_gender）。 */
    public static final String GENDER_MALE = "0";

    /** 性别：女。 */
    public static final String GENDER_FEMALE = "1";

    /** 性别：未知（用户表默认值）。 */
    public static final String GENDER_UNKNOWN = "2";

    private CommonConstants() {
    }
}
