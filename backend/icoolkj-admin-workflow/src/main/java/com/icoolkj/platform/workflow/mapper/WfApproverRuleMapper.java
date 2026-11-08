package com.icoolkj.platform.workflow.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.icoolkj.platform.workflow.domain.WfApproverRuleEntity;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRulePageQuery;
import com.icoolkj.platform.workflow.vo.WorkflowApproverRuleVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WfApproverRuleMapper {

    @InterceptorIgnore(tenantLine = "true")
    List<WfApproverRuleEntity> selectByModelAndTenants(@Param("modelId") String modelId,
                                                       @Param("tenantIds") List<String> tenantIds);

    @InterceptorIgnore(tenantLine = "true")
    WfApproverRuleEntity selectById(@Param("id") String id);

    @InterceptorIgnore(tenantLine = "true")
    List<WorkflowApproverRuleVO> selectPage(WorkflowApproverRulePageQuery query);

    @InterceptorIgnore(tenantLine = "true")
    long countByQuery(WorkflowApproverRulePageQuery query);

    @InterceptorIgnore(tenantLine = "true")
    WfApproverRuleEntity selectByUniqueKey(@Param("modelId") String modelId,
                                           @Param("taskDefinitionKey") String taskDefinitionKey,
                                           @Param("tenantId") String tenantId);

    int insert(WfApproverRuleEntity entity);

    int updateById(WfApproverRuleEntity entity);

    int deleteById(@Param("id") String id);
}
