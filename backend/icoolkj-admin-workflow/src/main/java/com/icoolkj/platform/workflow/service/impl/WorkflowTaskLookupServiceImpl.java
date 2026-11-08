package com.icoolkj.platform.workflow.service.impl;

import com.icoolkj.platform.workflow.service.WorkflowTaskLookupService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class WorkflowTaskLookupServiceImpl implements WorkflowTaskLookupService {

    private final TaskService taskService;

    public WorkflowTaskLookupServiceImpl(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String findActiveTaskId(String processInstanceId, String userId) {
        if (!StringUtils.hasText(processInstanceId) || !StringUtils.hasText(userId)) {
            return null;
        }
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee(userId)
                .active()
                .singleResult();
        return task == null ? null : task.getId();
    }
}
