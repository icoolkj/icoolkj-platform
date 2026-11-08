package com.icoolkj.platform.system.role.service;

import com.icoolkj.platform.system.menu.vo.MenuVO;

import java.util.List;

public interface RoleMenuService {

    List<String> listMenuIdsByRole(String roleId);

    void assignMenus(String roleId, List<String> menuIds);

    /** 角色菜单授权树（不含平台专属菜单；走角色权限，非菜单管理权限） */
    List<MenuVO> listAssignableMenus(String roleId, String productCode);

}
