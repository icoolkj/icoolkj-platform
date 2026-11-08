package com.icoolkj.platform.workflow.vo;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 流程实例详情 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WorkflowInstanceDetailVO extends WorkflowInstanceVO {

    private List<WorkflowHistoricTaskVO> tasks;
    /**
     * 本实例版本的 design 快照（JSON）。审批页流程图优先用此渲染，再用 progressNodes 染色。
     * @see docs/architecture/29-workflow-design-json-vs-bpmn.md
     */
    private String designJson;
    /** 按节点 key 的运行投影（pending/active/completed/rejected） */
    private List<WorkflowProgressNodeVO> progressNodes;
    /** Same biz document across reject/resubmit cycles (oldest first). */
    private List<WorkflowProcessCycleVO> processCycles;
    /** 当前待办任务 ID（审批人视角） */
    private String currentTaskId;
    private Map<String, Object> variables;

    /** 运行态行为（版本快照）：是否允许发起人撤回 */
    private Boolean allowWithdraw;
    /** 运行态行为：通过时是否强制意见 */
    private Boolean commentRequired;
    /** 运行态通知：是否允许催办 */
    private Boolean onUrge;
}
