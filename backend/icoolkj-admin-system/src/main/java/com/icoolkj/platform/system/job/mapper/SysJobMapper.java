package com.icoolkj.platform.system.job.mapper;

import com.icoolkj.platform.system.job.domain.SysJobEntity;
import com.icoolkj.platform.system.job.dto.SysJobPageQuery;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysJobMapper {

    SysJobEntity selectById(@Param("id") String id);

    SysJobEntity selectByJobCode(@Param("jobCode") String jobCode);

    List<SysJobEntity> selectPage(SysJobPageQuery query);

    long countByQuery(SysJobPageQuery query);

    List<SysJobEntity> selectAllEnabled();

    int insert(SysJobEntity entity);

    int updateById(SysJobEntity entity);
}
