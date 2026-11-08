package com.icoolkj.platform.oa.mapper;

import com.icoolkj.platform.oa.domain.OaInstancePromoteEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OaInstancePromoteMapper {

    List<OaInstancePromoteEntity> selectByInstanceId(@Param("instanceId") String instanceId);

    int insertBatch(@Param("list") List<OaInstancePromoteEntity> list);

    int deleteByInstanceId(@Param("instanceId") String instanceId);
}
