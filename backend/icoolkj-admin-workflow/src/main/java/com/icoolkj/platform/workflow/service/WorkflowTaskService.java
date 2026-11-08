package com.icoolkj.platform.workflow.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.workflow.dto.WorkflowTaskAddSignRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskCompleteRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskDelegateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskJumpRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskPageQuery;
import com.icoolkj.platform.workflow.dto.WorkflowTaskRemoveSignRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskTransferRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskUrgeRequest;
import com.icoolkj.platform.workflow.vo.WorkflowTaskDetailVO;
import com.icoolkj.platform.workflow.vo.WorkflowTaskVO;

/** 工作流任务服务 */
public interface WorkflowTaskService {

    PageResult<WorkflowTaskVO> pageTodo(WorkflowTaskPageQuery query);

    PageResult<WorkflowTaskVO> pageDone(WorkflowTaskPageQuery query);

    /** 抄送给我的：未读（活跃）+ 已阅（历史） */
    PageResult<WorkflowTaskVO> pageCc(WorkflowTaskPageQuery query);

    WorkflowTaskDetailVO getByTaskId(String taskId);

    void completeTask(String taskId, WorkflowTaskCompleteRequest request);

    void transferTask(String taskId, WorkflowTaskTransferRequest request);

    void delegateTask(String taskId, WorkflowTaskDelegateRequest request);

    void jumpTask(String taskId, WorkflowTaskJumpRequest request);

    void urgeTask(String taskId, WorkflowTaskUrgeRequest request);

    void addSignTask(String taskId, WorkflowTaskAddSignRequest request);

    void removeSignTask(String taskId, WorkflowTaskRemoveSignRequest request);

    /** 标记抄送任务已读 */
    void markCcRead(String taskId);
}
