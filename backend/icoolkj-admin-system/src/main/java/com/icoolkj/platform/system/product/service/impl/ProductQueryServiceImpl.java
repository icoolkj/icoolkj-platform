package com.icoolkj.platform.system.product.service.impl;

import com.icoolkj.platform.common.constant.ProductCodes;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.product.domain.ProductEntity;
import com.icoolkj.platform.system.menu.mapper.MenuMapper;
import com.icoolkj.platform.system.product.mapper.ProductMapper;
import com.icoolkj.platform.system.menu.service.MenuQueryService;
import com.icoolkj.platform.system.product.service.ProductQueryService;
import com.icoolkj.platform.system.menu.support.MenuNameI18nHelper;
import com.icoolkj.platform.system.product.vo.ProductVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductQueryServiceImpl implements ProductQueryService {

    private static final Map<String, String> HOME_PATHS = Map.of(
            ProductCodes.PLATFORM, "/product/platform/overview/index",
            ProductCodes.OA, "/product/oa/overview/index",
            ProductCodes.FILES, "/product/files/overview/index",
            ProductCodes.AI, "/product/ai/overview/index"
    );

    private final ProductMapper productMapper;
    private final MenuMapper menuMapper;
    private final MenuQueryService menuQueryService;

    public ProductQueryServiceImpl(ProductMapper productMapper,
                                   MenuMapper menuMapper,
                                   MenuQueryService menuQueryService) {
        this.productMapper = productMapper;
        this.menuMapper = menuMapper;
        this.menuQueryService = menuQueryService;
    }

    @Override
    public List<ProductVO> listMine(LoginUser loginUser, String locale) {
        Set<String> granted = menuQueryService.loadGrantedMenuIds(loginUser);
        if (granted == null || granted.isEmpty()) {
            return List.of();
        }
        List<String> codes = menuMapper.selectDistinctProductCodesByMenuIds(granted);
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        return toVos(productMapper.selectActiveByCodes(codes), locale);
    }

    @Override
    public List<ProductVO> listAllActive(String locale) {
        return toVos(productMapper.selectAllActive(), locale);
    }

    private List<ProductVO> toVos(List<ProductEntity> entities, String locale) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        List<ProductVO> list = new ArrayList<>(entities.size());
        for (ProductEntity entity : entities) {
            ProductVO vo = new ProductVO();
            vo.setCode(entity.getCode());
            vo.setName(MenuNameI18nHelper.resolve(locale, entity.getNameI18n(), entity.getName()));
            vo.setIcon(entity.getIcon());
            vo.setSortNo(entity.getSortNo());
            String home = HOME_PATHS.get(entity.getCode());
            vo.setHomePath(StringUtils.hasText(home) ? home : "/" + entity.getCode() + "/overview");
            list.add(vo);
        }
        return list;
    }
}
