package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.InfraFileCatalogItemEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InfraFileCatalogItemMapper {

    List<InfraFileCatalogItemEntity> selectByTenantAndCatalog(@Param("tenantId") String tenantId,
                                                              @Param("catalogCode") String catalogCode);

    InfraFileCatalogItemEntity selectByTenantCatalogProfile(@Param("tenantId") String tenantId,
                                                            @Param("catalogCode") String catalogCode,
                                                            @Param("profileCode") String profileCode);

    int insert(InfraFileCatalogItemEntity entity);

    int updateById(InfraFileCatalogItemEntity entity);

    int logicalDeleteById(@Param("id") String id);

    int deleteByTenantAndCatalog(@Param("tenantId") String tenantId, @Param("catalogCode") String catalogCode);
}
