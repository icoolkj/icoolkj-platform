package com.icoolkj.platform.system.product.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.system.menu.mapper.MenuMapper;
import com.icoolkj.platform.system.product.convert.ProductConvert;
import com.icoolkj.platform.system.product.domain.ProductEntity;
import com.icoolkj.platform.system.product.dto.ProductCreateRequest;
import com.icoolkj.platform.system.product.dto.ProductPageQuery;
import com.icoolkj.platform.system.product.dto.ProductUpdateRequest;
import com.icoolkj.platform.system.product.mapper.ProductMapper;
import com.icoolkj.platform.system.product.service.ProductService;
import com.icoolkj.platform.system.product.vo.ProductManageVO;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 产品宇宙管理服务实现 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final MenuMapper menuMapper;

    public ProductServiceImpl(ProductMapper productMapper, MenuMapper menuMapper) {
        this.productMapper = productMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public PageResult<ProductManageVO> page(ProductPageQuery query) {
        long total = productMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<ProductEntity> list = productMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        return PageResult.of(
                list.stream().map(ProductConvert::toManageVo).toList(),
                total,
                query.getPageNo(),
                query.getPageSize());
    }

    @Override
    public ProductManageVO getById(String id) {
        ProductEntity entity = productMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.product");
        }
        return ProductConvert.toManageVo(entity);
    }

    @Override
    public String create(ProductCreateRequest request) {
        assertCodeAvailable(request.getCode(), null);
        ProductEntity entity = ProductConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        productMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(ProductUpdateRequest request) {
        ProductEntity entity = productMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.product");
        }
        ProductConvert.copy(request, entity);
        productMapper.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        ProductEntity entity = productMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.product");
        }
        if (ProductConvert.isBuiltin(entity.getCode())) {
            throw new BusinessException("error.product.builtin_protected");
        }
        long menuCount = menuMapper.countByProductCode(entity.getCode());
        if (menuCount > 0) {
            throw new BusinessException("error.product.has_menus", entity.getCode());
        }
        AuditFieldHelper.markDeleted(entity);
        productMapper.updateById(entity);
    }

    private void assertCodeAvailable(String code, String excludeId) {
        if (!StringUtils.hasText(code)) {
            return;
        }
        ProductEntity existing = productMapper.selectByCode(code.trim());
        if (existing == null) {
            return;
        }
        if (excludeId == null || !excludeId.equals(existing.getId())) {
            throw new BusinessException("error.product.code_exists");
        }
    }
}
