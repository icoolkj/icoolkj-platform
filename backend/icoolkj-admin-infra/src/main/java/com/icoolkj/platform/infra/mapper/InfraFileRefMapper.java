package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.InfraFileRefEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InfraFileRefMapper {

    InfraFileRefEntity selectActive(@Param("tenantId") String tenantId,
                                    @Param("fileId") String fileId,
                                    @Param("bizType") String bizType,
                                    @Param("bizId") String bizId,
                                    @Param("refField") String refField);

    long countActiveByFileId(@Param("tenantId") String tenantId, @Param("fileId") String fileId);

    List<InfraFileRefEntity> selectActiveByFileId(@Param("tenantId") String tenantId, @Param("fileId") String fileId);

    int insert(InfraFileRefEntity entity);

    int logicalDeleteById(@Param("id") String id);

    int logicalDelete(@Param("tenantId") String tenantId,
                      @Param("fileId") String fileId,
                      @Param("bizType") String bizType,
                      @Param("bizId") String bizId,
                      @Param("refField") String refField);

    int deleteById(@Param("id") String id);

    int deleteByFileId(@Param("tenantId") String tenantId, @Param("fileId") String fileId);

    List<InfraFileRefEntity> selectActiveByBiz(@Param("tenantId") String tenantId,
                                               @Param("bizType") String bizType,
                                               @Param("bizId") String bizId);
}
