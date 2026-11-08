package com.icoolkj.platform.workflow.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** BPMN 用户任务节点进度（用于 Steps 对比） */
@Data
public class WorkflowProgressNodeVO {

    /** 节点定义 key（BPMN userTask id） */
    private String nodeKey;
    private String nodeName;
    /** pending | active | completed | rejected | skipped */
    private String nodeStatus;
    private String assigneeUserId;
    private String assigneeUserName;
    private Boolean approved;
    private String comment;
    private String taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
