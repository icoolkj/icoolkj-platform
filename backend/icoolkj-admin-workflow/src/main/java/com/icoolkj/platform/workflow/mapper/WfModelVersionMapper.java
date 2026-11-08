package com.icoolkj.platform.workflow.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.icoolkj.platform.workflow.domain.WfModelVersionEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WfModelVersionMapper {

    @InterceptorIgnore(tenantLine = "true")
    WfModelVersionEntity selectById(@Param("id") String id);

    @InterceptorIgnore(tenantLine = "true")
    WfModelVersionEntity selectPublishedByModelId(@Param("modelId") String modelId);

    @InterceptorIgnore(tenantLine = "true")
    WfModelVersionEntity selectLatestByModelId(@Param("modelId") String modelId);

    @InterceptorIgnore(tenantLine = "true")
    List<WfModelVersionEntity> selectByModelId(@Param("modelId") String modelId);

    @InterceptorIgnore(tenantLine = "true")
    Integer selectMaxVersionNo(@Param("modelId") String modelId);

    @InterceptorIgnore(tenantLine = "true")
    WfModelVersionEntity selectByProcessDefinitionId(@Param("processDefinitionId") String processDefinitionId);

    int insert(WfModelVersionEntity entity);

    int updateById(WfModelVersionEntity entity);

    @InterceptorIgnore(tenantLine = "true")
    int unpublishByModelId(@Param("modelId") String modelId);
}
