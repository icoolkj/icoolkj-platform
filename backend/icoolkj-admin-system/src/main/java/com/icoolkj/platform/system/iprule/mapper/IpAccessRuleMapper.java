package com.icoolkj.platform.system.iprule.mapper;

import com.icoolkj.platform.system.iprule.domain.IpAccessRuleEntity;
import com.icoolkj.platform.system.iprule.dto.IpAccessRulePageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IpAccessRuleMapper {

    IpAccessRuleEntity selectById(@Param("id") String id);

    List<IpAccessRuleEntity> selectList(IpAccessRulePageQuery query);

    List<IpAccessRuleEntity> selectPage(IpAccessRulePageQuery query);

    long countByQuery(IpAccessRulePageQuery query);

    int insert(IpAccessRuleEntity entity);

    int updateById(IpAccessRuleEntity entity);

    int deleteById(@Param("id") String id);

    List<IpAccessRuleEntity> selectActiveRules(@Param("platformTenantId") String platformTenantId,
            @Param("tenantId") String tenantId, @Param("now") LocalDateTime now);
}
