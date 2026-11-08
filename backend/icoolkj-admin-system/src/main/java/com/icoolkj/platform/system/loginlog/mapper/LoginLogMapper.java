package com.icoolkj.platform.system.loginlog.mapper;

import com.icoolkj.platform.system.loginlog.domain.LoginLogEntity;
import com.icoolkj.platform.system.loginlog.dto.LoginLogPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LoginLogMapper {

    LoginLogEntity selectById(@Param("id") String id);

    List<LoginLogEntity> selectList(LoginLogEntity query);

    List<LoginLogEntity> selectPage(LoginLogPageQuery query);

    long countByQuery(LoginLogPageQuery query);

    int insert(LoginLogEntity entity);

    int updateById(LoginLogEntity entity);

    int deleteById(@Param("id") String id);

    int purgeBefore(@Param("before") LocalDateTime before);

    String selectLatestRowHash(@Param("tenantId") String tenantId);

    int archiveBefore(@Param("before") LocalDateTime before);

    int purgeArchiveBefore(@Param("before") LocalDateTime before);

    List<String> selectTenantIdsBefore(@Param("before") LocalDateTime before);

    List<LoginLogEntity> selectHashedByTenantOrdered(@Param("tenantId") String tenantId);

    int updateHashes(@Param("id") String id,
                     @Param("prevHash") String prevHash,
                     @Param("rowHash") String rowHash);

}
