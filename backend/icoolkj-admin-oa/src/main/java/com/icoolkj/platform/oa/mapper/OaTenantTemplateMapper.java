package com.icoolkj.platform.oa.mapper;

import com.icoolkj.platform.oa.domain.OaTenantTemplateEntity;
import com.icoolkj.platform.oa.dto.OaTenantTemplatePageQuery;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OaTenantTemplateMapper {

    OaTenantTemplateEntity selectById(@Param("id") String id);

    OaTenantTemplateEntity selectByCategoryCode(@Param("tenantId") String tenantId, @Param("categoryCode") String categoryCode);

    List<OaTenantTemplateEntity> selectPage(OaTenantTemplatePageQuery query);

    long countByQuery(OaTenantTemplatePageQuery query);

    /** 发起中心：已启用且已发布的模板 */
    List<OaTenantTemplateEntity> selectLaunchable(@Param("tenantId") String tenantId,
                                                  @Param("enableStatus") String enableStatus);

    int insert(OaTenantTemplateEntity entity);

    int updateById(OaTenantTemplateEntity entity);
}
