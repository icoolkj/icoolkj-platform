package com.icoolkj.platform.workflow.vo;

import lombok.Data;

/** 流程模型部署结果 */
@Data
public class WorkflowModelDeployResultVO {

    private String modelId;
    private String versionId;
    private Integer versionNo;
    private String deploymentId;
    private String processDefinitionId;
    private String processDefinitionKey;
}
