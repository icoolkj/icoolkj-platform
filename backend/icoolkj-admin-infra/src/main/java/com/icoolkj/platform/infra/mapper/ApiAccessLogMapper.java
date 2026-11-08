package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.ApiAccessLogEntity;
import com.icoolkj.platform.infra.dto.ApiAccessLogPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ApiAccessLogMapper {

    ApiAccessLogEntity selectById(@Param("id") String id);

    List<ApiAccessLogEntity> selectList(ApiAccessLogEntity query);

    List<ApiAccessLogEntity> selectPage(ApiAccessLogPageQuery query);

    long countByQuery(ApiAccessLogPageQuery query);

    int insert(ApiAccessLogEntity entity);

    int updateById(ApiAccessLogEntity entity);

    int deleteById(@Param("id") String id);

    int purgeBefore(@Param("before") LocalDateTime before);

    String selectLatestRowHash(@Param("tenantId") String tenantId);

    int archiveBefore(@Param("before") LocalDateTime before);

    int purgeArchiveBefore(@Param("before") LocalDateTime before);

    List<String> selectTenantIdsBefore(@Param("before") LocalDateTime before);

    List<ApiAccessLogEntity> selectHashedByTenantOrdered(@Param("tenantId") String tenantId);

    int updateHashes(@Param("id") String id,
                     @Param("prevHash") String prevHash,
                     @Param("rowHash") String rowHash);

}
