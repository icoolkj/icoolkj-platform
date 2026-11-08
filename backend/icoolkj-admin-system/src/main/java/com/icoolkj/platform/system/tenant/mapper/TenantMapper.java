package com.icoolkj.platform.system.tenant.mapper;

import com.icoolkj.platform.system.tenant.domain.TenantEntity;
import com.icoolkj.platform.system.tenant.dto.TenantPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TenantMapper {

    TenantEntity selectById(@Param("id") String id);

    List<TenantEntity> selectList(TenantPageQuery query);

    List<TenantEntity> selectPage(TenantPageQuery query);

    long countByQuery(TenantPageQuery query);

    int insert(TenantEntity entity);

    int updateById(TenantEntity entity);

    int deleteById(@Param("id") String id);

    TenantEntity selectByTenantCode(@Param("tenantCode") String tenantCode);

}
