package com.icoolkj.platform.system.menu.service.impl;

import com.icoolkj.platform.common.constant.MenuAssignScope;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.system.menu.mapper.MenuMapper;
import com.icoolkj.platform.system.role.mapper.RoleMenuMapper;
import com.icoolkj.platform.system.menu.service.MenuAssignScopeService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class MenuAssignScopeServiceImpl implements MenuAssignScopeService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    public MenuAssignScopeServiceImpl(MenuMapper menuMapper, RoleMenuMapper roleMenuMapper) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public boolean isPlatformTenant(String tenantId) {
        return TenantConstants.PLATFORM_TENANT_ID.equals(tenantId);
    }

    @Override
    public boolean isAssignableByTenant(String tenantId, String menuId) {
        if (!StringUtils.hasText(menuId)) {
            return false;
        }
        if (isPlatformTenant(tenantId)) {
            return true;
        }
        String scope = menuMapper.selectAssignScopeById(menuId);
        return MenuAssignScope.isTenant(scope);
    }

    @Override
    public boolean containsNonAssignableMenus(String tenantId, Collection<String> menuIds) {
        if (isPlatformTenant(tenantId) || CollectionUtils.isEmpty(menuIds)) {
            return false;
        }
        return menuMapper.countByMenuIdsAndAssignScope(menuIds, MenuAssignScope.PLATFORM) > 0;
    }

    @Override
    public List<String> filterAssignableMenuIds(String tenantId, Collection<String> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }
        if (isPlatformTenant(tenantId)) {
            return new ArrayList<>(menuIds);
        }
        return menuIds.stream().filter(id -> isAssignableByTenant(tenantId, id)).toList();
    }

    @Override
    public List<String> listTenantAssignableMenuIds() {
        return menuMapper.selectActiveMenuIdsByAssignScope(MenuAssignScope.TENANT);
    }

    @Override
    public List<String> listTenantAssignablePermissionCodes() {
        return menuMapper.selectActivePermissionCodesByAssignScope(MenuAssignScope.TENANT);
    }

    @Override
    public void assertPlatformMenuOperator() {
        String tenantId = SecurityContextHelper.getTenantId();
        if (!isPlatformTenant(tenantId)) {
            throw new BusinessException("error.menu.platform_operator_only");
        }
    }

    @Override
    public void onMenuMarkedPlatform(String menuId) {
        if (!StringUtils.hasText(menuId)) {
            return;
        }
        roleMenuMapper.deleteByMenuIdForNonPlatformTenants(menuId);
    }
}
