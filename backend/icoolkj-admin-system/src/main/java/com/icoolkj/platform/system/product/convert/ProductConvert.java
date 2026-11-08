package com.icoolkj.platform.system.product.convert;

import com.icoolkj.platform.common.constant.ProductCodes;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.system.menu.support.MenuNameI18nHelper;
import com.icoolkj.platform.system.product.domain.ProductEntity;
import com.icoolkj.platform.system.product.dto.ProductCreateRequest;
import com.icoolkj.platform.system.product.dto.ProductUpdateRequest;
import com.icoolkj.platform.system.product.vo.ProductManageVO;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;

/** 产品宇宙对象转换 */
public final class ProductConvert {

    private static final Set<String> BUILTIN_CODES = Set.of(
            ProductCodes.PLATFORM, ProductCodes.OA, ProductCodes.FILES, ProductCodes.AI);

    private ProductConvert() {
    }

    public static boolean isBuiltin(String code) {
        return StringUtils.hasText(code) && BUILTIN_CODES.contains(code.trim());
    }

    public static ProductManageVO toManageVo(ProductEntity entity) {
        if (entity == null) {
            return null;
        }
        ProductManageVO vo = new ProductManageVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        Map<String, String> i18n = MenuNameI18nHelper.parse(entity.getNameI18n());
        vo.setNameEn(i18n.get(LocaleResolver.LOCALE_EN_US));
        vo.setIcon(entity.getIcon());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setBuiltin(isBuiltin(entity.getCode()));
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static ProductEntity toEntity(ProductCreateRequest request) {
        ProductEntity entity = new ProductEntity();
        entity.setCode(request.getCode().trim());
        entity.setName(request.getName().trim());
        entity.setNameI18n(buildNameI18n(request.getName(), request.getNameEn()));
        entity.setIcon(trimToNull(request.getIcon()));
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(trimToNull(request.getRemark()));
        return entity;
    }

    public static void copy(ProductUpdateRequest request, ProductEntity entity) {
        entity.setName(request.getName().trim());
        entity.setNameI18n(buildNameI18n(request.getName(), request.getNameEn()));
        entity.setIcon(trimToNull(request.getIcon()));
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(trimToNull(request.getRemark()));
    }

    private static String buildNameI18n(String name, String nameEn) {
        Map<String, String> map = new LinkedHashMap<>();
        if (StringUtils.hasText(nameEn)) {
            map.put(LocaleResolver.LOCALE_EN_US, nameEn.trim());
        }
        return MenuNameI18nHelper.buildJson(map, name);
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
