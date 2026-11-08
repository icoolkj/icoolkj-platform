package com.icoolkj.platform.workflow.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 流程实例列表项 */
@Data
public class WorkflowInstanceVO {

    private String instanceId;
    private String processDefinitionKey;
    private String processDefinitionName;
    private String bizType;
    private String bizId;
    private String bizTitle;
    private String status;
    private String starterUserId;
    private String starterUserName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 当前活动节点名称（多个用顿号拼接） */
    private String currentTaskName;
    /** 当前办理人显示名（未解析到时为空） */
    private String currentAssigneeUserNames;
    /** 运行中且无有效办理人 */
    private Boolean assigneeUnresolved;
    /** 业务办理路由（查看详情） */
    private String formRoute;
    /** 运行态 behavior.allowWithdraw；非 running 为 false */
    private Boolean allowWithdraw;
    /**
     * 业务表审批/归档状态（如 OA/公告 approve_status）。
     * 与 {@link #status}（引擎流程态）分离：关闭只改业务态，不改流程态。
     */
    private String bizApproveStatus;
}
