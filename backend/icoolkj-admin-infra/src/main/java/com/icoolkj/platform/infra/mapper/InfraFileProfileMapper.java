package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.InfraFileProfileEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InfraFileProfileMapper {

    InfraFileProfileEntity selectByTenantAndCode(@Param("tenantId") String tenantId,
                                                 @Param("profileCode") String profileCode);

    List<InfraFileProfileEntity> selectByTenant(@Param("tenantId") String tenantId,
                                                @Param("categoryTag") String categoryTag,
                                                @Param("keyword") String keyword);

    int insert(InfraFileProfileEntity entity);

    int updateById(InfraFileProfileEntity entity);

    int logicalDeleteById(@Param("id") String id);
}
