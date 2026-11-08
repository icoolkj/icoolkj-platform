package com.icoolkj.platform.workflow.service;

/** 查询当前用户在某流程实例上的活动任务 */
public interface WorkflowTaskLookupService {

    String findActiveTaskId(String processInstanceId, String userId);
}
