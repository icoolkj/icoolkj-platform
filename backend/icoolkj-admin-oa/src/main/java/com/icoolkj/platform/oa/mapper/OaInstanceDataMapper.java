package com.icoolkj.platform.oa.mapper;

import com.icoolkj.platform.oa.domain.OaInstanceDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OaInstanceDataMapper {

    OaInstanceDataEntity selectByInstanceId(@Param("instanceId") String instanceId);

    int insert(OaInstanceDataEntity entity);

    int updateById(OaInstanceDataEntity entity);

    int deleteByInstanceId(@Param("instanceId") String instanceId);
}
