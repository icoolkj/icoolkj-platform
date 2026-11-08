package com.icoolkj.platform.workflow.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 工作流任务详情 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WorkflowTaskDetailVO extends WorkflowTaskVO {

    private List<WorkflowCommentVO> commentList;
    private Map<String, Object> variables;

    /** end | previous | starter | starterEdit */
    private String rejectMode;
    private Boolean allowTransfer;
    private Boolean allowDelegate;
    private Boolean allowAddSign;
    /**
     * 会签/或签且节点允许加签时，可减签其他活跃办理人。
     * 与 {@link #allowAddSign} 同源设计开关；仅 multi-instance 节点为 true。
     */
    private Boolean allowRemoveSign;
    /** 本节点其他活跃并行办理人（不含当前任务办理人） */
    private List<WorkflowParallelAssigneeVO> parallelAssignees;
}
