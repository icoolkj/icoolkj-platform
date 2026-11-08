package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.ThirdPartyCallLogEntity;
import com.icoolkj.platform.infra.dto.ThirdPartyCallLogPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ThirdPartyCallLogMapper {

    ThirdPartyCallLogEntity selectById(@Param("id") String id);

    List<ThirdPartyCallLogEntity> selectList(ThirdPartyCallLogEntity query);

    List<ThirdPartyCallLogEntity> selectPage(ThirdPartyCallLogPageQuery query);

    long countByQuery(ThirdPartyCallLogPageQuery query);

    int insert(ThirdPartyCallLogEntity entity);

    int updateById(ThirdPartyCallLogEntity entity);

    int deleteById(@Param("id") String id);

}
