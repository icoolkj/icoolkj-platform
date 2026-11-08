package com.icoolkj.platform.workflow.service;

import com.icoolkj.platform.workflow.deploy.WorkflowVersionPublishCommand;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.vo.WorkflowModelDeployResultVO;

/** 流程模型注册与解析 */
public interface WorkflowModelRegistry {

    WfModelEntity requireById(String modelId);

    WfModelEntity requireByModelKey(String modelKey);

    WfModelEntity requireActiveByBizType(String tenantId, String bizType);

    String resolveFormRoute(String processDefinitionKey);

    String resolveModelName(String processDefinitionKey);

    WorkflowModelDeployResultVO publishVersion(WorkflowVersionPublishCommand command);

    default WorkflowModelDeployResultVO publishVersion(String modelId, String bpmnXml, String remark) {
        return publishVersion(WorkflowVersionPublishCommand.bpmnOnly(modelId, bpmnXml, remark));
    }
}
