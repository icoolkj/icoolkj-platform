package com.icoolkj.platform.oa.mapper;

import com.icoolkj.platform.oa.domain.OaTenantFieldDefEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OaTenantFieldDefMapper {

    List<OaTenantFieldDefEntity> selectByVersionId(@Param("versionId") String versionId);

    int deleteByVersionId(@Param("versionId") String versionId);

    int insertBatch(@Param("list") List<OaTenantFieldDefEntity> list);
}
