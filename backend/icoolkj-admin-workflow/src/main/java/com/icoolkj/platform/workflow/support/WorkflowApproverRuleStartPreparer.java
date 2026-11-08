package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.workflow.spi.WorkflowStartContext;
import com.icoolkj.platform.workflow.spi.WorkflowStartPreparer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/** 按 wf_approver_rule 注入审批人流程变量（已改为运行时按节点解析，启动不再全量预解析） */
@Component
@Order(0)
public class WorkflowApproverRuleStartPreparer implements WorkflowStartPreparer {

    @Override
    public boolean supports(String bizType) {
        return true;
    }

    @Override
    public Map<String, Object> prepareVariables(WorkflowStartContext context) {
        // 办理人改在节点到达时由 WorkflowAssignee*Listener 解析，避免未走到的条件分支导致整单无法发起
        return Map.of();
    }
}
