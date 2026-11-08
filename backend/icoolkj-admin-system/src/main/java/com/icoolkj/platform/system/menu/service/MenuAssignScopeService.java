package com.icoolkj.platform.system.menu.service;

import java.util.Collection;
import java.util.List;

/** 基于 {@code sys_menu.assign_scope} 的菜单授权范围判定 */
public interface MenuAssignScopeService {

    boolean isPlatformTenant(String tenantId);

    boolean isAssignableByTenant(String tenantId, String menuId);

    boolean containsNonAssignableMenus(String tenantId, Collection<String> menuIds);

    List<String> filterAssignableMenuIds(String tenantId, Collection<String> menuIds);

    List<String> listTenantAssignableMenuIds();

    List<String> listTenantAssignablePermissionCodes();

    void assertPlatformMenuOperator();

    void onMenuMarkedPlatform(String menuId);
}
