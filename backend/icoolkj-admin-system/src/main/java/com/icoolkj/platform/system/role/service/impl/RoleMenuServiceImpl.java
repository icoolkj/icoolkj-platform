package com.icoolkj.platform.system.role.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.MenuAssignScope;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.system.menu.convert.MenuConvert;
import com.icoolkj.platform.system.menu.domain.MenuEntity;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.role.domain.RoleMenuEntity;
import com.icoolkj.platform.system.user.domain.UserRoleEntity;
import com.icoolkj.platform.system.menu.dto.MenuPageQuery;
import com.icoolkj.platform.system.menu.mapper.MenuMapper;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import com.icoolkj.platform.system.role.mapper.RoleMenuMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import com.icoolkj.platform.system.menu.service.MenuAssignScopeService;
import com.icoolkj.platform.system.role.service.RoleMenuService;
import com.icoolkj.platform.system.menu.vo.MenuVO;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RoleMenuServiceImpl implements RoleMenuService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final MenuAssignScopeService menuAssignScopeService;
    private final UserRoleMapper userRoleMapper;
    private final LoginSessionService loginSessionService;

    public RoleMenuServiceImpl(RoleMapper roleMapper,
                               RoleMenuMapper roleMenuMapper,
                               MenuMapper menuMapper,
                               MenuAssignScopeService menuAssignScopeService,
                               UserRoleMapper userRoleMapper,
                               LoginSessionService loginSessionService) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
        this.menuAssignScopeService = menuAssignScopeService;
        this.userRoleMapper = userRoleMapper;
        this.loginSessionService = loginSessionService;
    }

    @Override
    public List<String> listMenuIdsByRole(String roleId) {
        RoleEntity role = requireRole(roleId);
        assertOperatorCanManageRole(role);
        RoleMenuEntity query = new RoleMenuEntity();
        query.setTenantId(role.getTenantId());
        query.setRoleId(roleId);
        List<RoleMenuEntity> rows = roleMenuMapper.selectList(query);
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyList();
        }
        return menuAssignScopeService.filterAssignableMenuIds(
                role.getTenantId(),
                rows.stream().map(RoleMenuEntity::getMenuId).toList());
    }

    @Override
    public List<MenuVO> listAssignableMenus(String roleId, String productCode) {
        RoleEntity role = requireRole(roleId);
        assertOperatorCanManageRole(role);
        MenuPageQuery query = new MenuPageQuery();
        query.setPageNo(1L);
        query.setPageSize(10000L);
        query.setStatus(CommonConstants.STATUS_NORMAL);
        if (StringUtils.hasText(productCode)) {
            query.setProductCode(productCode.trim());
        }
        if (!menuAssignScopeService.isPlatformTenant(role.getTenantId())) {
            query.setAssignScope(MenuAssignScope.TENANT);
        }
        List<MenuEntity> menus = menuMapper.selectPage(query);
        if (CollectionUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }
        Locale locale = LocaleContextHolder.getLocale();
        String localeTag = locale != null ? locale.toLanguageTag() : null;
        return menus.stream().map(m -> MenuConvert.toLocalizedVo(m, localeTag)).toList();
    }

    @Override
    public void assignMenus(String roleId, List<String> menuIds) {
        RoleEntity role = requireRole(roleId);
        assertOperatorCanManageRole(role);
        String tenantId = role.getTenantId();
        if (menuAssignScopeService.containsNonAssignableMenus(tenantId, menuIds)) {
            throw new BusinessException("error.role.menu_assign_platform_only");
        }
        List<String> safeMenuIds = menuAssignScopeService.filterAssignableMenuIds(tenantId, menuIds);

        RoleMenuEntity deleteQuery = new RoleMenuEntity();
        deleteQuery.setTenantId(tenantId);
        deleteQuery.setRoleId(roleId);
        roleMenuMapper.delete(deleteQuery);
        if (!CollectionUtils.isEmpty(safeMenuIds)) {
            for (String menuId : safeMenuIds) {
                if (menuId == null || menuId.isBlank()) {
                    continue;
                }
                RoleMenuEntity entity = new RoleMenuEntity();
                entity.setTenantId(tenantId);
                entity.setRoleId(roleId);
                entity.setMenuId(menuId);
                roleMenuMapper.insert(entity);
            }
        }
        revokeSessionsForRoleUsers(tenantId, roleId);
    }

    private void revokeSessionsForRoleUsers(String tenantId, String roleId) {
        UserRoleEntity query = new UserRoleEntity();
        query.setTenantId(tenantId);
        query.setRoleId(roleId);
        List<UserRoleEntity> rows = userRoleMapper.selectList(query);
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        Set<String> userIds = new HashSet<>();
        for (UserRoleEntity row : rows) {
            if (row != null && StringUtils.hasText(row.getUserId())) {
                userIds.add(row.getUserId());
            }
        }
        for (String userId : userIds) {
            loginSessionService.removeAllUserSessions(tenantId, userId);
        }
    }

    private RoleEntity requireRole(String roleId) {
        RoleEntity role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.role");
        }
        return role;
    }

    private void assertOperatorCanManageRole(RoleEntity role) {
        String operatorTenantId = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(operatorTenantId)) {
            return;
        }
        if (menuAssignScopeService.isPlatformTenant(operatorTenantId)) {
            return;
        }
        if (!operatorTenantId.equals(role.getTenantId())) {
            throw new BusinessException("error.forbidden");
        }
    }
}
