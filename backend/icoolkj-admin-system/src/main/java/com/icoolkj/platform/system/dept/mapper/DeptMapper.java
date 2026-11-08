package com.icoolkj.platform.system.dept.mapper;

import com.icoolkj.platform.system.dept.domain.DeptEntity;
import com.icoolkj.platform.system.dept.dto.DeptPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeptMapper {

    DeptEntity selectById(@Param("id") String id);

    List<DeptEntity> selectList(DeptEntity query);

    List<DeptEntity> selectPage(DeptPageQuery query);

    long countByQuery(DeptPageQuery query);

    int insert(DeptEntity entity);

    int updateById(DeptEntity entity);

    int deleteById(@Param("id") String id);

    List<DeptEntity> selectChildren(@Param("tenantId") String tenantId, @Param("parentId") String parentId);

}
