package com.icoolkj.platform.system.product.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.product.dto.ProductCreateRequest;
import com.icoolkj.platform.system.product.dto.ProductPageQuery;
import com.icoolkj.platform.system.product.dto.ProductUpdateRequest;
import com.icoolkj.platform.system.product.vo.ProductManageVO;

/** 产品宇宙管理（平台元数据） */
public interface ProductService {

    PageResult<ProductManageVO> page(ProductPageQuery query);

    ProductManageVO getById(String id);

    String create(ProductCreateRequest request);

    void update(ProductUpdateRequest request);

    void deleteById(String id);
}
