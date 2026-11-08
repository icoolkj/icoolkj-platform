package com.icoolkj.platform.system.role.mapper;

import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.role.dto.RolePageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    RoleEntity selectById(@Param("id") String id);

    List<RoleEntity> selectList(RoleEntity query);

    List<RoleEntity> selectPage(RolePageQuery query);

    long countByQuery(RolePageQuery query);

    int insert(RoleEntity entity);

    int updateById(RoleEntity entity);

    int deleteById(@Param("id") String id);

}
