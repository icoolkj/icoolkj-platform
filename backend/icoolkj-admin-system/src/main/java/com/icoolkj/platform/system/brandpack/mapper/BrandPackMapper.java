package com.icoolkj.platform.system.brandpack.mapper;

import com.icoolkj.platform.system.brandpack.domain.BrandPackEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BrandPackMapper {

    BrandPackEntity selectByTenantId(@Param("tenantId") String tenantId);

    int insert(BrandPackEntity entity);

    int updateById(BrandPackEntity entity);

    int deleteById(@Param("id") String id);
}
