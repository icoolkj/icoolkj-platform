package com.icoolkj.platform.system.dict.mapper;

import com.icoolkj.platform.system.dict.domain.DictTypeEntity;
import com.icoolkj.platform.system.dict.dto.DictTypePageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictTypeMapper {

    DictTypeEntity selectById(@Param("id") String id);

    List<DictTypeEntity> selectList(DictTypeEntity query);

    List<DictTypeEntity> selectPage(DictTypePageQuery query);

    long countByQuery(DictTypePageQuery query);

    int insert(DictTypeEntity entity);

    int updateById(DictTypeEntity entity);

    int deleteById(@Param("id") String id);

    List<DictTypeEntity> selectActiveByTenant(@Param("tenantId") String tenantId);

}
