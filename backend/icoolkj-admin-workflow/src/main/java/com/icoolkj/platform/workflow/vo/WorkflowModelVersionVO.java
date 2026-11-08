package com.icoolkj.platform.workflow.vo;

import lombok.Data;

/** 流程模型版本 */
@Data
public class WorkflowModelVersionVO {

    private String id;
    private String modelId;
    private Integer versionNo;
    private String deploymentId;
    private String processDefinitionId;
    private String processDefinitionKey;
    private Boolean published;
    /** 是否已固化 design_json（列表不返回全文） */
    private Boolean hasDesignJson;
    private String remark;
}
