package com.icoolkj.platform.workflow.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 历史任务节点 */
@Data
public class WorkflowHistoricTaskVO {

    private String taskId;
    private String taskName;
    /** BPMN 节点 id */
    private String taskDefinitionKey;
    /** completed | running | pending | cancelled */
    private String nodeStatus;
    private String assigneeUserId;
    private String assigneeUserName;
    private Boolean approved;
    private String comment;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** Process cycle within the same biz document (1-based). */
    private Integer cycleNo;
    private String processInstanceId;
    /**
     * 审计动作类型（转办/委派/撤回等）；空=普通审批任务。
     * 见 {@link com.icoolkj.platform.workflow.constant.WfTaskActionTypes} 及 delegate_return 等展示扩展值。
     */
    private String actionType;
    /**
     * 系统动作原因码（业务模块自定义 snake_case，如写入 payload 的 reasonCode）；
     * 前端 gen i18n 解析，不进 sys_i18n_message。
     */
    private String reasonCode;
    /** 动作目标人（转办/委派对象） */
    private String toUserId;
    private String toUserName;
}
