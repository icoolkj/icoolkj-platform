package com.icoolkj.platform.system.menu.convert;

import com.icoolkj.platform.common.constant.MenuAssignScope;
import com.icoolkj.platform.common.constant.ProductCodes;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.menu.domain.MenuEntity;
import com.icoolkj.platform.system.menu.dto.MenuCreateRequest;
import com.icoolkj.platform.system.menu.dto.MenuUpdateRequest;
import com.icoolkj.platform.system.menu.support.MenuNameI18nHelper;
import com.icoolkj.platform.system.menu.vo.MenuVO;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/** 菜单权限资源表对象转换 */
public final class MenuConvert {

    private MenuConvert() {
    }

    public static MenuVO toVo(MenuEntity entity) {
        if (entity == null) {
            return null;
        }
        MenuVO vo = new MenuVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setMenuType(entity.getMenuType());
        vo.setMenuName(entity.getMenuName());
        vo.setNameI18n(MenuNameI18nHelper.parse(entity.getNameI18n()));
        vo.setRouteName(entity.getRouteName());
        vo.setRoutePath(entity.getRoutePath());
        vo.setComponentPath(entity.getComponentPath());
        vo.setPermissionCode(entity.getPermissionCode());
        vo.setIcon(entity.getIcon());
        vo.setSortNo(entity.getSortNo());
        vo.setVisible(entity.getVisible());
        vo.setStatus(entity.getStatus());
        vo.setIsExternal(entity.getIsExternal());
        vo.setIsCache(entity.getIsCache());
        vo.setRemark(entity.getRemark());
        vo.setAssignScope(entity.getAssignScope());
        vo.setProductCode(entity.getProductCode());
        return vo;
    }

    /** Same as {@link #toVo} but {@code menuName} resolved from {@code name_i18n} for the request locale. */
    public static MenuVO toLocalizedVo(MenuEntity entity, String locale) {
        MenuVO vo = toVo(entity);
        if (vo == null) {
            return null;
        }
        vo.setMenuName(MenuNameI18nHelper.resolve(locale, entity.getNameI18n(), entity.getMenuName()));
        return vo;
    }

    public static MenuEntity toEntity(MenuCreateRequest request) {
        MenuEntity entity = new MenuEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setParentId(request.getParentId());
        entity.setMenuType(request.getMenuType());
        applyNameFields(entity, request.getMenuName(), request.getNameI18n());
        entity.setRouteName(request.getRouteName());
        entity.setRoutePath(request.getRoutePath());
        entity.setComponentPath(request.getComponentPath());
        entity.setPermissionCode(request.getPermissionCode());
        entity.setIcon(request.getIcon());
        entity.setSortNo(request.getSortNo());
        entity.setVisible(request.getVisible());
        entity.setStatus(request.getStatus());
        entity.setIsExternal(request.getIsExternal());
        entity.setIsCache(request.getIsCache());
        entity.setRemark(request.getRemark());
        entity.setAssignScope(resolveAssignScope(request.getAssignScope()));
        entity.setProductCode(resolveProductCode(request.getProductCode()));
        return entity;
    }

    public static void copy(MenuUpdateRequest request, MenuEntity entity) {
        entity.setParentId(request.getParentId());
        entity.setMenuType(request.getMenuType());
        applyNameFields(entity, request.getMenuName(), request.getNameI18n());
        entity.setRouteName(request.getRouteName());
        entity.setRoutePath(request.getRoutePath());
        entity.setComponentPath(request.getComponentPath());
        entity.setPermissionCode(request.getPermissionCode());
        entity.setIcon(request.getIcon());
        entity.setSortNo(request.getSortNo());
        entity.setVisible(request.getVisible());
        entity.setStatus(request.getStatus());
        entity.setIsExternal(request.getIsExternal());
        entity.setIsCache(request.getIsCache());
        entity.setRemark(request.getRemark());
        if (request.getAssignScope() != null) {
            entity.setAssignScope(resolveAssignScope(request.getAssignScope()));
        }
        if (request.getProductCode() != null) {
            entity.setProductCode(resolveProductCode(request.getProductCode()));
        }
    }

    private static void applyNameFields(MenuEntity entity, String menuName, Map<String, String> nameI18n) {
        Map<String, String> map = new LinkedHashMap<>();
        if (nameI18n != null) {
            map.putAll(nameI18n);
        }
        String zh = StringUtils.hasText(menuName) ? menuName.trim() : map.get(LocaleResolver.LOCALE_ZH_CN);
        if (StringUtils.hasText(zh)) {
            map.put(LocaleResolver.LOCALE_ZH_CN, zh);
            entity.setMenuName(zh);
        } else {
            entity.setMenuName(menuName);
        }
        entity.setNameI18n(MenuNameI18nHelper.buildJson(map, entity.getMenuName()));
    }

    private static String resolveAssignScope(String assignScope) {
        return MenuAssignScope.isPlatform(assignScope) ? MenuAssignScope.PLATFORM : MenuAssignScope.TENANT;
    }

    private static String resolveProductCode(String productCode) {
        if (!StringUtils.hasText(productCode)) {
            return ProductCodes.PLATFORM;
        }
        return productCode.trim();
    }
}
