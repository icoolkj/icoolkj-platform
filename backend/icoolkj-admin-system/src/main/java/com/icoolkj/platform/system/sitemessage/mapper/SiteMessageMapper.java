package com.icoolkj.platform.system.sitemessage.mapper;

import com.icoolkj.platform.system.sitemessage.domain.SiteMessageEntity;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessagePageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SiteMessageMapper {

    SiteMessageEntity selectById(@Param("id") String id);

    List<SiteMessageEntity> selectPage(SiteMessagePageQuery query);

    long countByQuery(SiteMessagePageQuery query);

    int insert(SiteMessageEntity entity);

    int deleteById(@Param("id") String id);

    int deleteByIds(@Param("ids") List<String> ids);
}
