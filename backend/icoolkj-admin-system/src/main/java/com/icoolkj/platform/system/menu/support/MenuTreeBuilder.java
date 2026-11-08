package com.icoolkj.platform.system.menu.support;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.system.menu.domain.MenuEntity;
import com.icoolkj.platform.system.menu.vo.MenuRouterVO;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 将扁平菜单列表构建为路由树 */
public final class MenuTreeBuilder {

    private MenuTreeBuilder() {
    }

    public static List<MenuRouterVO> buildTree(List<MenuEntity> menus, Set<String> grantedMenuIds) {
        return buildTree(menus, grantedMenuIds, null);
    }

    public static List<MenuRouterVO> buildTree(List<MenuEntity> menus,
                                               Set<String> grantedMenuIds,
                                               String locale) {
        if (menus == null || menus.isEmpty()) {
            return List.of();
        }
        Set<String> visibleIds = resolveVisibleMenuIds(menus, grantedMenuIds);
        Map<String, MenuRouterVO> nodeMap = new HashMap<>();
        for (MenuEntity menu : menus) {
            if (!visibleIds.contains(menu.getId())) {
                continue;
            }
            nodeMap.put(menu.getId(), toRouter(menu, locale));
        }
        List<MenuRouterVO> roots = new ArrayList<>();
        for (MenuEntity menu : menus) {
            if (!visibleIds.contains(menu.getId())) {
                continue;
            }
            MenuRouterVO current = nodeMap.get(menu.getId());
            String parentId = menu.getParentId();
            if (!StringUtils.hasText(parentId) || CommonConstants.TREE_ROOT_PARENT_ID.equals(parentId)) {
                roots.add(current);
                continue;
            }
            MenuRouterVO parent = nodeMap.get(parentId);
            if (parent != null) {
                parent.getChildren().add(current);
            } else {
                roots.add(current);
            }
        }
        sortTree(roots);
        return roots;
    }

    private static Set<String> resolveVisibleMenuIds(List<MenuEntity> menus, Set<String> grantedMenuIds) {
        Map<String, MenuEntity> menuMap = menus.stream()
                .collect(Collectors.toMap(MenuEntity::getId, m -> m, (a, b) -> a));
        Set<String> visible = new HashSet<>();
        for (String menuId : grantedMenuIds) {
            MenuEntity menu = menuMap.get(menuId);
            if (menu == null) {
                continue;
            }
            visible.add(menuId);
            addAncestors(menu, menuMap, visible);
        }
        for (MenuEntity menu : menus) {
            if ("M".equals(menu.getMenuType()) && hasGrantedDescendant(menu.getId(), menus, grantedMenuIds)) {
                visible.add(menu.getId());
                addAncestors(menu, menuMap, visible);
            }
        }
        return visible;
    }

    private static boolean hasGrantedDescendant(String menuId, List<MenuEntity> menus, Set<String> grantedMenuIds) {
        for (MenuEntity menu : menus) {
            if (!menuId.equals(menu.getParentId())) {
                continue;
            }
            if (grantedMenuIds.contains(menu.getId())) {
                return true;
            }
            if (hasGrantedDescendant(menu.getId(), menus, grantedMenuIds)) {
                return true;
            }
        }
        return false;
    }

    private static void addAncestors(MenuEntity menu, Map<String, MenuEntity> menuMap, Set<String> visible) {
        String parentId = menu.getParentId();
        while (StringUtils.hasText(parentId) && !CommonConstants.TREE_ROOT_PARENT_ID.equals(parentId)) {
            visible.add(parentId);
            MenuEntity parent = menuMap.get(parentId);
            if (parent == null) {
                break;
            }
            parentId = parent.getParentId();
        }
    }

    private static MenuRouterVO toRouter(MenuEntity menu, String locale) {
        MenuRouterVO vo = new MenuRouterVO();
        vo.setId(menu.getId());
        vo.setMenuName(MenuNameI18nHelper.resolve(locale, menu.getNameI18n(), menu.getMenuName()));
        vo.setRouteName(menu.getRouteName());
        vo.setIcon(menu.getIcon());
        vo.setPermissionCode(menu.getPermissionCode());
        vo.setMenuType(menu.getMenuType());
        vo.setVisible(menu.getVisible());
        vo.setSortNo(menu.getSortNo());
        vo.setProductCode(menu.getProductCode());
        vo.setComponent(menu.getComponentPath());
        vo.setPath(buildPath(menu));
        return vo;
    }

    private static String buildPath(MenuEntity menu) {
        if ("M".equals(menu.getMenuType())) {
            return normalizePath(menu.getRoutePath());
        }
        if (StringUtils.hasText(menu.getComponentPath())) {
            return normalizePath(menu.getComponentPath());
        }
        return normalizePath(menu.getRoutePath());
    }

    private static String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        String value = path.trim();
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        if (value.endsWith("/index")) {
            value = value.substring(0, value.length() - "/index".length());
        }
        return value.replaceAll("/+", "/");
    }

    private static void sortTree(List<MenuRouterVO> nodes) {
        nodes.sort(Comparator.comparing(MenuRouterVO::getSortNo, Comparator.nullsLast(Integer::compareTo)));
        for (MenuRouterVO node : nodes) {
            if (!node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }
}
