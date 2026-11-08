package com.icoolkj.platform.common.constant;

/**
 * 菜单授权范围 — 存于 {@code sys_menu.assign_scope}，由平台管理员在菜单管理维护。
 * <ul>
 *   <li>{@link #PLATFORM} — 仅平台可管理/授权（菜单模板、平台字典参数、租户管理、AI 配置等）</li>
 *   <li>{@link #TENANT} — 租户角色可授权、租户超级角色默认可用</li>
 * </ul>
 */
public final class MenuAssignScope {

    public static final String PLATFORM = "PLATFORM";
    public static final String TENANT = "TENANT";

    private MenuAssignScope() {
    }

    public static boolean isPlatform(String assignScope) {
        return PLATFORM.equals(assignScope);
    }

    public static boolean isTenant(String assignScope) {
        return assignScope == null || assignScope.isBlank() || TENANT.equals(assignScope);
    }
}
