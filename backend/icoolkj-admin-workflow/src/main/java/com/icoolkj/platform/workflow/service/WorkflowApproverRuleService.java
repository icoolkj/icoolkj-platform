package com.icoolkj.platform.workflow.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRuleCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRulePageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowApproverRuleUpdateRequest;
import com.icoolkj.platform.workflow.vo.WorkflowApproverRuleVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelOptionVO;
import java.util.List;

public interface WorkflowApproverRuleService {

    PageResult<WorkflowApproverRuleVO> page(WorkflowApproverRulePageQuery query);

    WorkflowApproverRuleVO getById(String id);

    List<WorkflowModelOptionVO> listModelOptions();

    String create(WorkflowApproverRuleCreateRequest request);

    void update(WorkflowApproverRuleUpdateRequest request);

    void delete(String id);
}
