package com.icoolkj.platform.workflow.deploy;

import org.springframework.util.StringUtils;

/**
 * 发布一版流程模型：BPMN 必填；designJson 为版本 Design SoT；
 * processConfigJson 为全局配置快照（startAcl/behavior/notify），缺省时由 Registry 回落模型草稿。
 */
public record WorkflowVersionPublishCommand(
        String modelId,
        String bpmnXml,
        String designJson,
        String remark,
        String processConfigJson
) {

    public static WorkflowVersionPublishCommand bpmnOnly(String modelId, String bpmnXml, String remark) {
        return new WorkflowVersionPublishCommand(modelId, bpmnXml, null, remark, null);
    }

    public WorkflowVersionPublishCommand {
        if (!StringUtils.hasText(modelId)) {
            throw new IllegalArgumentException("modelId required");
        }
        if (!StringUtils.hasText(bpmnXml)) {
            throw new IllegalArgumentException("bpmnXml required");
        }
    }
}
