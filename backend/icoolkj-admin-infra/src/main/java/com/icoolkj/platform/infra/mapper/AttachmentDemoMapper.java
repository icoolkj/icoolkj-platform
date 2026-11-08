package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.AttachmentDemoEntity;
import com.icoolkj.platform.infra.dto.AttachmentDemoPageQuery;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AttachmentDemoMapper {

    AttachmentDemoEntity selectById(@Param("id") String id);

    List<AttachmentDemoEntity> selectPage(AttachmentDemoPageQuery query);

    long countByQuery(AttachmentDemoPageQuery query);

    int insert(AttachmentDemoEntity entity);

    int updateById(AttachmentDemoEntity entity);
}
