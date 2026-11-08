package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.InfraFileCatalogEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InfraFileCatalogMapper {

    InfraFileCatalogEntity selectByTenantAndCode(@Param("tenantId") String tenantId,
                                                 @Param("catalogCode") String catalogCode);

    List<InfraFileCatalogEntity> selectByTenant(@Param("tenantId") String tenantId);

    int insert(InfraFileCatalogEntity entity);

    int updateById(InfraFileCatalogEntity entity);

    int logicalDeleteById(@Param("id") String id);
}
