package com.icoolkj.platform.system.auth.service.impl;

import com.icoolkj.platform.framework.auth.PermissionService;
import com.icoolkj.platform.system.menu.mapper.MenuMapper;
import com.icoolkj.platform.system.role.mapper.RoleMenuMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import com.icoolkj.platform.system.menu.service.MenuAssignScopeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.icoolkj.platform.system.role.mapper.RoleMapper;

import java.util.Collections;
import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final MenuAssignScopeService menuAssignScopeService;

    public PermissionServiceImpl(UserRoleMapper userRoleMapper,
                                 RoleMenuMapper roleMenuMapper,
                                 MenuMapper menuMapper,
                                 MenuAssignScopeService menuAssignScopeService) {
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
        this.menuAssignScopeService = menuAssignScopeService;
    }

    @Override
    public List<String> loadPermissionCodes(String tenantId, String userId, boolean superAdmin) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(userId)) {
            return Collections.emptyList();
        }

        if (superAdmin) {
            if (menuAssignScopeService.isPlatformTenant(tenantId)) {
                return menuMapper.selectActivePermissionCodesForPlatformSuperAdmin();
            }
            return menuMapper.selectAllActivePermissionCodes();
        }

        if (userRoleMapper.existsSuperRoleByTenantAndUser(tenantId, userId)) {
            return menuAssignScopeService.listTenantAssignablePermissionCodes();
        }

        List<String> roleIds = userRoleMapper.selectRoleIdsByTenantAndUser(tenantId, userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> menuIds = roleMenuMapper.selectMenuIdsByTenantAndRoleIds(tenantId, roleIds);
        if (menuIds == null || menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> permissionCodes = menuMapper.selectPermissionCodesByMenuIds(menuIds);
        return permissionCodes == null ? Collections.emptyList() : permissionCodes;
    }
}
