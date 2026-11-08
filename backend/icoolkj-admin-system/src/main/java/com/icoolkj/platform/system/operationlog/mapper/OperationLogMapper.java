package com.icoolkj.platform.system.operationlog.mapper;

import com.icoolkj.platform.system.operationlog.domain.OperationLogEntity;
import com.icoolkj.platform.system.operationlog.dto.OperationLogPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperationLogMapper {

    OperationLogEntity selectById(@Param("id") String id);

    List<OperationLogEntity> selectList(OperationLogEntity query);

    List<OperationLogEntity> selectPage(OperationLogPageQuery query);

    long countByQuery(OperationLogPageQuery query);

    int insert(OperationLogEntity entity);

    int updateById(OperationLogEntity entity);

    int deleteById(@Param("id") String id);

    int purgeBefore(@Param("before") LocalDateTime before);

    String selectLatestRowHash(@Param("tenantId") String tenantId);

    int archiveBefore(@Param("before") LocalDateTime before);

    int purgeArchiveBefore(@Param("before") LocalDateTime before);

    List<String> selectTenantIdsBefore(@Param("before") LocalDateTime before);

    List<OperationLogEntity> selectHashedByTenantOrdered(@Param("tenantId") String tenantId);

    int updateHashes(@Param("id") String id,
                     @Param("prevHash") String prevHash,
                     @Param("rowHash") String rowHash);

}
