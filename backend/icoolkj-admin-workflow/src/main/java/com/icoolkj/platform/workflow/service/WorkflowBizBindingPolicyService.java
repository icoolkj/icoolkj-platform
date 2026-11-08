package com.icoolkj.platform.workflow.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyCreateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyPageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowBizBindingPolicyUpdateRequest;
import com.icoolkj.platform.workflow.vo.WorkflowBizBindingPolicyVO;

/** 业务绑定策略管理 */
public interface WorkflowBizBindingPolicyService {

    PageResult<WorkflowBizBindingPolicyVO> page(WorkflowBizBindingPolicyPageQuery query);

    WorkflowBizBindingPolicyVO getById(String id);

    String create(WorkflowBizBindingPolicyCreateRequest request);

    void update(WorkflowBizBindingPolicyUpdateRequest request);

    void delete(String id);
}
