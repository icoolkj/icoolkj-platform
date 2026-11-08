package com.icoolkj.platform.workflow.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.icoolkj.platform.workflow.domain.WfBizBindingPolicyEntity;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyPageQuery;
import com.icoolkj.platform.workflow.vo.WorkflowBizBindingPolicyVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WfBizBindingPolicyMapper {

    @InterceptorIgnore(tenantLine = "true")
    WfBizBindingPolicyEntity selectByBizType(@Param("bizType") String bizType);

    @InterceptorIgnore(tenantLine = "true")
    WfBizBindingPolicyEntity selectById(@Param("id") String id);

    @InterceptorIgnore(tenantLine = "true")
    List<WorkflowBizBindingPolicyVO> selectPage(WorkflowBizBindingPolicyPageQuery query);

    @InterceptorIgnore(tenantLine = "true")
    long countByQuery(WorkflowBizBindingPolicyPageQuery query);

    int insert(WfBizBindingPolicyEntity entity);

    int updateById(WfBizBindingPolicyEntity entity);

    int deleteById(@Param("id") String id);
}
