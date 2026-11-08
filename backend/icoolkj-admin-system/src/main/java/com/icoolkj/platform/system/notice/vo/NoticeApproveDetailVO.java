package com.icoolkj.platform.system.notice.vo;

import com.icoolkj.platform.workflow.vo.WorkflowInstanceDetailVO;
import lombok.Data;

/** 公告审批页详情（完整业务数据 + 流程轨迹） */
@Data
public class NoticeApproveDetailVO {

    /** 公告完整 VO（含正文、封面等） */
    private NoticeVO notice;
    /** 当前审批人待办任务 ID */
    private String taskId;
    private String processInstanceId;
    private String approveStatus;
    /** 流程实例详情：节点进度 + 审批时间线 */
    private WorkflowInstanceDetailVO instance;
}
