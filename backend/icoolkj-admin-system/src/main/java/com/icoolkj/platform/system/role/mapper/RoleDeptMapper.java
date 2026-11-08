package com.icoolkj.platform.system.role.mapper;

import com.icoolkj.platform.system.role.domain.RoleDeptEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleDeptMapper {

    List<RoleDeptEntity> selectList(RoleDeptEntity query);

    int insert(RoleDeptEntity entity);

    int delete(RoleDeptEntity entity);

}
