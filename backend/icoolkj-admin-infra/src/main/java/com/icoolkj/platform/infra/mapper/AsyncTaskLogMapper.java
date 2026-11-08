package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.AsyncTaskLogEntity;
import com.icoolkj.platform.infra.dto.AsyncTaskLogPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AsyncTaskLogMapper {

    AsyncTaskLogEntity selectById(@Param("id") String id);

    List<AsyncTaskLogEntity> selectList(AsyncTaskLogEntity query);

    List<AsyncTaskLogEntity> selectPage(AsyncTaskLogPageQuery query);

    long countByQuery(AsyncTaskLogPageQuery query);

    int insert(AsyncTaskLogEntity entity);

    int updateById(AsyncTaskLogEntity entity);

    int deleteById(@Param("id") String id);

}
