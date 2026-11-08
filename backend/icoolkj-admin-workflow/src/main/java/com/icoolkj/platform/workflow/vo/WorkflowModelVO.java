package com.icoolkj.platform.workflow.vo;

import lombok.Data;

/** 流程模型列表项 */
@Data
public class WorkflowModelVO {

    private String id;
    /** 归属租户；0=平台预置 */
    private String tenantId;
    private String modelKey;
    private String modelName;
    private String categoryId;
    private String categoryName;
    private String bizType;
    private String formType;
    private String formRoute;
    private Integer latestVersion;
    private String status;
    private String remark;
    /** 流程全局配置 JSON */
    private String processConfig;
}
