package com.icoolkj.platform.workflow.support;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 业务单据是否已产生审批历史（有历史 → 禁删、可关）。
 */
@Component
public class WorkflowBizHistorySupport {

    private final WorkflowBizProcessLookup workflowBizProcessLookup;

    public WorkflowBizHistorySupport(WorkflowBizProcessLookup workflowBizProcessLookup) {
        this.workflowBizProcessLookup = workflowBizProcessLookup;
    }

    /**
     * @param processInstanceId 业务表当前/最近流程实例 ID（可空）
     * @param bizType           wf_biz_process.biz_type
     * @param bizId             业务主键
     */
    public boolean hasApprovalHistory(String processInstanceId, String bizType, String bizId) {
        if (StringUtils.hasText(processInstanceId)) {
            return true;
        }
        return workflowBizProcessLookup.findLatestByBiz(bizType, bizId) != null;
    }
}
