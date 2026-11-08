package com.icoolkj.platform.system.job.mapper;

import com.icoolkj.platform.system.job.domain.SysJobLogEntity;
import com.icoolkj.platform.system.job.dto.SysJobLogPageQuery;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysJobLogMapper {

    SysJobLogEntity selectById(@Param("id") String id);

    List<SysJobLogEntity> selectPage(SysJobLogPageQuery query);

    long countByQuery(SysJobLogPageQuery query);

    long countRunningByJobCode(@Param("jobCode") String jobCode);

    int insert(SysJobLogEntity entity);

    int updateById(SysJobLogEntity entity);
}
