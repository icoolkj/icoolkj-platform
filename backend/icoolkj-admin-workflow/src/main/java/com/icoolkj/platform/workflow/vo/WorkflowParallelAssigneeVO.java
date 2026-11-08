package com.icoolkj.platform.workflow.vo;

import lombok.Data;

/** 会签/或签节点当前活跃并行办理人（供减签选择） */
@Data
public class WorkflowParallelAssigneeVO {

    private String taskId;
    private String userId;
    private String userName;
}
