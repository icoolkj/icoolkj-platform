package com.icoolkj.platform.system.product.mapper;

import com.icoolkj.platform.system.product.domain.ProductEntity;
import com.icoolkj.platform.system.product.dto.ProductPageQuery;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductMapper {

    ProductEntity selectById(@Param("id") String id);

    ProductEntity selectByCode(@Param("code") String code);

    List<ProductEntity> selectActiveByCodes(@Param("codes") Collection<String> codes);

    List<ProductEntity> selectAllActive();

    List<ProductEntity> selectPage(ProductPageQuery query);

    long countByQuery(ProductPageQuery query);

    int insert(ProductEntity entity);

    int updateById(ProductEntity entity);
}
