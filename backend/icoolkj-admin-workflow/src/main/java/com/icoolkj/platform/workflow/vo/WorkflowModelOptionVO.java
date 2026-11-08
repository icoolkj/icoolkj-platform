package com.icoolkj.platform.workflow.vo;

import lombok.Data;

/** 流程模型下拉选项 */
@Data
public class WorkflowModelOptionVO {

    private String value;
    private String label;
    private String modelKey;
    private String bizType;
}
