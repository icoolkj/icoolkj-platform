package com.icoolkj.platform.workflow.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 工作流任务列表项 */
@Data
public class WorkflowTaskVO {

    private String taskId;
    private String taskName;
    private String taskDefinitionKey;
    private String processInstanceId;
    private String processDefinitionKey;
    private String processDefinitionName;
    private String bizType;
    private String bizId;
    private String bizTitle;
    private String starterUserId;
    private String starterUserName;
    private String formRoute;
    private LocalDateTime createTime;
    private LocalDateTime dueDate;
    /** 已办任务完成时间 */
    private LocalDateTime endTime;
    /** 审批结果（字典 wf_approve_result：pass/reject） */
    private String approveResult;
    /** 是否抄送任务（列表/详情用于入口分流） */
    private Boolean ccTask;
    /** 抄送是否已读（仅 ccTask=true 时有意义） */
    private Boolean ccRead;
}
