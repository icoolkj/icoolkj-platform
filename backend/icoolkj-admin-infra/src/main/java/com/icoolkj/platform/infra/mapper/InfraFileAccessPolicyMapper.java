package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.InfraFileAccessPolicyEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InfraFileAccessPolicyMapper {

    List<InfraFileAccessPolicyEntity> selectEnabled();

    List<InfraFileAccessPolicyEntity> selectAll();

    InfraFileAccessPolicyEntity selectById(@Param("id") String id);

    InfraFileAccessPolicyEntity selectByPattern(@Param("bizTypePattern") String bizTypePattern);

    int insert(InfraFileAccessPolicyEntity entity);

    int updateById(InfraFileAccessPolicyEntity entity);

    int logicalDeleteById(@Param("id") String id,
                          @Param("updatedBy") String updatedBy,
                          @Param("updatedAt") LocalDateTime updatedAt);
}
