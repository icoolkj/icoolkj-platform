package com.icoolkj.platform.system.dict.mapper;

import com.icoolkj.platform.system.dict.domain.DictItemEntity;
import com.icoolkj.platform.system.dict.dto.DictItemPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictItemMapper {

    DictItemEntity selectById(@Param("id") String id);

    List<DictItemEntity> selectList(DictItemEntity query);

    List<DictItemEntity> selectPage(DictItemPageQuery query);

    long countByQuery(DictItemPageQuery query);

    int insert(DictItemEntity entity);

    int updateById(DictItemEntity entity);

    int deleteById(@Param("id") String id);

    /** Active dict items for a dict type key (tenant + platform fallback). */
    List<DictItemEntity> selectActiveByDictType(@Param("dictType") String dictType, @Param("tenantId") String tenantId);

}
