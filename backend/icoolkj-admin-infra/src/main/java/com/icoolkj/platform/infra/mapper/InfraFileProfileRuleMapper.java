package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.InfraFileProfileRuleEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InfraFileProfileRuleMapper {

    InfraFileProfileRuleEntity selectByTenantAndCode(@Param("tenantId") String tenantId,
                                                     @Param("profileCode") String profileCode);

    List<InfraFileProfileRuleEntity> selectByTenant(@Param("tenantId") String tenantId);

    int insert(InfraFileProfileRuleEntity entity);

    int updateById(InfraFileProfileRuleEntity entity);

    int logicalDeleteById(@Param("id") String id);

    int deleteByTenantAndCode(@Param("tenantId") String tenantId, @Param("profileCode") String profileCode);
}
