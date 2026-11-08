package com.icoolkj.platform.oa.mapper;

import com.icoolkj.platform.oa.domain.OaPlatformTemplateVersionEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OaPlatformTemplateVersionMapper {

    OaPlatformTemplateVersionEntity selectById(@Param("id") String id);

    OaPlatformTemplateVersionEntity selectDraftByTemplateId(@Param("templateId") String templateId);

    OaPlatformTemplateVersionEntity selectPublishedByTemplateId(@Param("templateId") String templateId);

    List<OaPlatformTemplateVersionEntity> selectByTemplateId(@Param("templateId") String templateId);

    Integer selectMaxVersionNo(@Param("templateId") String templateId);

    int insert(OaPlatformTemplateVersionEntity entity);

    int updateById(OaPlatformTemplateVersionEntity entity);

    int archivePublishedByTemplateId(@Param("templateId") String templateId);
}
