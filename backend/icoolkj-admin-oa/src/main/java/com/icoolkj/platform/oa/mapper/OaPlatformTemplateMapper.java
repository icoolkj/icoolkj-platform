package com.icoolkj.platform.oa.mapper;

import com.icoolkj.platform.oa.domain.OaPlatformTemplateEntity;
import com.icoolkj.platform.oa.dto.OaPlatformTemplatePageQuery;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateOptionVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OaPlatformTemplateMapper {

    OaPlatformTemplateEntity selectById(@Param("id") String id);

    OaPlatformTemplateEntity selectByCategoryCode(@Param("tenantId") String tenantId, @Param("categoryCode") String categoryCode);

    OaPlatformTemplateEntity selectByTemplateCode(@Param("tenantId") String tenantId, @Param("templateCode") String templateCode);

    List<OaPlatformTemplateEntity> selectPage(OaPlatformTemplatePageQuery query);

    long countByQuery(OaPlatformTemplatePageQuery query);

    List<OaPlatformTemplateOptionVO> selectPublishedOptions(@Param("tenantId") String tenantId);

    int insert(OaPlatformTemplateEntity entity);

    int updateById(OaPlatformTemplateEntity entity);
}
