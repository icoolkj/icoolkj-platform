package com.icoolkj.platform.workflow.service;

import com.icoolkj.platform.workflow.dto.WorkflowStartRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskAddSignRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskCompleteRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskDelegateRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskJumpRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskRemoveSignRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskTransferRequest;
import com.icoolkj.platform.workflow.dto.WorkflowTaskUrgeRequest;
import java.util.Map;

/** Flowable 流程编排 */
public interface WorkflowProcessService {

    String startProcess(WorkflowStartRequest request);

    void completeTask(String taskId, WorkflowTaskCompleteRequest request);

    void transferTask(String taskId, WorkflowTaskTransferRequest request);

    void delegateTask(String taskId, WorkflowTaskDelegateRequest request);

    /** 管理员自由跳转至指定 activity（非驳回语义） */
    void jumpTask(String taskId, WorkflowTaskJumpRequest request);

    void urgeTask(String taskId, WorkflowTaskUrgeRequest request);

    void addSignTask(String taskId, WorkflowTaskAddSignRequest request);

    /** 会签/或签减签：删除其他活跃 multi-instance 办理人 */
    void removeSignTask(String taskId, WorkflowTaskRemoveSignRequest request);

    void cancelProcess(String processInstanceId, String reason);

    /** 管理员终止实例（流程监控；与发起人撤回区分审计动作） */
    void terminateProcess(String processInstanceId, String reason);

    /**
     * starterEdit 退回后发起人重提：激活挂起实例并清除 {@code wfStarterEditPending}。
     */
    void resubmitFromStarterEdit(String processInstanceId);

    Map<String, Object> getProcessVariables(String processInstanceId);

    /**
     * 流程已结束后同步 {@code wf_biz_process}（幂等）。
     * 供 completeTask 与引擎 PROCESS_COMPLETED 事件共用，避免超时直达结束漏写。
     */
    void syncBizStatusIfEnded(String processInstanceId);

    /**
     * callActivity 子流程启动桥接：写入/补齐 {@code wf_biz_process.parent_instance_id}。
     */
    void linkSubprocessInstance(String childProcessInstanceId, String parentProcessInstanceId);
}
