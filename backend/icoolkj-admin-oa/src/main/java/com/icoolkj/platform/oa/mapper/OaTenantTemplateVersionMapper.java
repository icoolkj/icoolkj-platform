package com.icoolkj.platform.oa.mapper;

import com.icoolkj.platform.oa.domain.OaTenantTemplateVersionEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OaTenantTemplateVersionMapper {

    OaTenantTemplateVersionEntity selectById(@Param("id") String id);

    OaTenantTemplateVersionEntity selectDraftByTemplateId(@Param("templateId") String templateId);

    OaTenantTemplateVersionEntity selectPublishedByTemplateId(@Param("templateId") String templateId);

    List<OaTenantTemplateVersionEntity> selectByTemplateId(@Param("templateId") String templateId);

    Integer selectMaxVersionNo(@Param("templateId") String templateId);

    int insert(OaTenantTemplateVersionEntity entity);

    int updateById(OaTenantTemplateVersionEntity entity);

    int archivePublishedByTemplateId(@Param("templateId") String templateId);
}
