package com.icoolkj.platform.system.menu.support;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.ProductCodes;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.system.menu.domain.MenuEntity;
import com.icoolkj.platform.system.menu.mapper.MenuMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 产品宇宙菜单结构校验：M→C→F；产品由 Tab 表达，允许多个顶级 M/C */
@Component
public class MenuProductRules {

    private final MenuMapper menuMapper;

    public MenuProductRules(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    public void validateForCreate(MenuEntity entity) {
        validateStructure(entity, null);
    }

    public void validateForUpdate(MenuEntity entity, String entityId, String previousProductCode) {
        validateStructure(entity, entityId);
        if (previousProductCode != null
                && entity.getProductCode() != null
                && !previousProductCode.equals(entity.getProductCode())) {
            MenuEntity childQuery = new MenuEntity();
            childQuery.setParentId(entityId);
            if (!menuMapper.selectList(childQuery).isEmpty()) {
                throw new BusinessException("error.menu.product_change_has_children");
            }
        }
    }

    private void validateStructure(MenuEntity entity, String excludeId) {
        String parentId = entity.getParentId();
        boolean isRoot = !StringUtils.hasText(parentId)
                || CommonConstants.TREE_ROOT_PARENT_ID.equals(parentId);

        if (isRoot) {
            if ("F".equals(entity.getMenuType())) {
                throw new BusinessException("error.menu.root_no_button");
            }
            if (!"M".equals(entity.getMenuType()) && !"C".equals(entity.getMenuType())) {
                throw new BusinessException("error.menu.root_must_be_directory_or_page");
            }
            return;
        }

        MenuEntity parent = menuMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.menu");
        }
        validateMenuTypeHierarchy(entity.getMenuType(), parent.getMenuType());

        String childProduct = resolveProductCode(entity.getProductCode());
        String parentProduct = resolveProductCode(parent.getProductCode());
        if (!childProduct.equals(parentProduct)) {
            throw new BusinessException("error.menu.product_mismatch", parentProduct, childProduct);
        }
        if (entity.getProductCode() == null || !StringUtils.hasText(entity.getProductCode())) {
            entity.setProductCode(parentProduct);
        }
    }

    /** 目录(M) ← 目录(M)；菜单(C) ← 目录(M)；按钮(F) ← 菜单(C) */
    private static void validateMenuTypeHierarchy(String menuType, String parentMenuType) {
        if (!StringUtils.hasText(menuType) || !StringUtils.hasText(parentMenuType)) {
            return;
        }
        switch (menuType) {
            case "M" -> {
                if (!"M".equals(parentMenuType)) {
                    throw new BusinessException("error.menu.directory_parent_must_be_directory");
                }
            }
            case "C" -> {
                if (!"M".equals(parentMenuType)) {
                    throw new BusinessException("error.menu.page_parent_must_be_directory");
                }
            }
            case "F" -> {
                if (!"C".equals(parentMenuType)) {
                    throw new BusinessException("error.menu.button_parent_must_be_page");
                }
            }
            default -> {
            }
        }
    }

    private static String resolveProductCode(String productCode) {
        if (!StringUtils.hasText(productCode)) {
            return ProductCodes.PLATFORM;
        }
        return productCode.trim();
    }
}
