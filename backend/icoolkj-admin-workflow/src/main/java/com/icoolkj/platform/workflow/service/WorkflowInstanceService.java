package com.icoolkj.platform.workflow.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.workflow.dto.WorkflowInstanceCancelRequest;
import com.icoolkj.platform.workflow.dto.WorkflowInstancePageQuery;
import com.icoolkj.platform.workflow.vo.WorkflowInstanceDetailVO;
import com.icoolkj.platform.workflow.vo.WorkflowInstanceVO;

/** 工作流实例服务 */
public interface WorkflowInstanceService {

    /** 已发申请：每业务单据最新一轮主流程（历史轮次见详情 processCycles） */
    PageResult<WorkflowInstanceVO> pageSubmitted(WorkflowInstancePageQuery query);

    /** 本租户流程实例监控（管理员） */
    PageResult<WorkflowInstanceVO> pageTenant(WorkflowInstancePageQuery query);

    WorkflowInstanceDetailVO getByInstanceId(String instanceId);

    void cancelInstance(String instanceId);

    /** 管理员终止（流程监控） */
    void terminateInstance(String instanceId, WorkflowInstanceCancelRequest request);
}
