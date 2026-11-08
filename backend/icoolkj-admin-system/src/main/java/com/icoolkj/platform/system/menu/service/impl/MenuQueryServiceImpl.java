package com.icoolkj.platform.system.menu.service.impl;

import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.menu.domain.MenuEntity;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.menu.mapper.MenuMapper;
import com.icoolkj.platform.system.role.mapper.RoleMenuMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import com.icoolkj.platform.system.menu.service.MenuAssignScopeService;
import com.icoolkj.platform.system.menu.service.MenuQueryService;
import com.icoolkj.platform.system.menu.support.MenuTreeBuilder;
import com.icoolkj.platform.system.menu.vo.MenuRouterVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MenuQueryServiceImpl implements MenuQueryService {

    private final MenuMapper menuMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuAssignScopeService menuAssignScopeService;

    public MenuQueryServiceImpl(MenuMapper menuMapper,
                                UserMapper userMapper,
                                UserRoleMapper userRoleMapper,
                                RoleMenuMapper roleMenuMapper,
                                MenuAssignScopeService menuAssignScopeService) {
        this.menuMapper = menuMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuAssignScopeService = menuAssignScopeService;
    }

    @Override
    public List<MenuRouterVO> getRouters(LoginUser loginUser, String locale) {
        return getRouters(loginUser, locale, null);
    }

    @Override
    public List<MenuRouterVO> getRouters(LoginUser loginUser, String locale, String productCode) {
        if (loginUser == null || !StringUtils.hasText(loginUser.getUserId())) {
            return List.of();
        }
        List<MenuEntity> routerMenus = StringUtils.hasText(productCode)
                ? menuMapper.selectRouterMenusByProductCode(productCode.trim())
                : menuMapper.selectRouterMenus();
        Set<String> grantedMenuIds = loadGrantedMenuIds(loginUser);
        return MenuTreeBuilder.buildTree(routerMenus, grantedMenuIds, locale);
    }

    @Override
    public Set<String> loadGrantedMenuIds(LoginUser loginUser) {
        UserEntity user = userMapper.selectById(loginUser.getUserId());
        if (user != null && YesNoEnum.isYes(user.getIsSuperAdmin())) {
            if (menuAssignScopeService.isPlatformTenant(loginUser.getTenantId())) {
                return new HashSet<>(menuMapper.selectActiveMenuIdsForPlatformSuperAdmin());
            }
            return new HashSet<>(menuMapper.selectAllActiveMenuIds());
        }
        if (userRoleMapper.existsSuperRoleByTenantAndUser(loginUser.getTenantId(), loginUser.getUserId())) {
            return loadTenantSuperRoleMenuIds(loginUser.getTenantId());
        }
        List<String> roleIds = userRoleMapper.selectRoleIdsByTenantAndUser(loginUser.getTenantId(), loginUser.getUserId());
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<String> menuIds = roleMenuMapper.selectMenuIdsByTenantAndRoleIds(loginUser.getTenantId(), roleIds);
        return menuIds == null ? Collections.emptySet() : new HashSet<>(menuIds);
    }

    private Set<String> loadTenantSuperRoleMenuIds(String tenantId) {
        return new HashSet<>(menuAssignScopeService.listTenantAssignableMenuIds());
    }
}
