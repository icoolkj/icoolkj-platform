package com.icoolkj.platform.workflow.vo;

import lombok.Data;

/** 租户可同步的平台模型目录项 */
@Data
public class WorkflowModelSyncCatalogVO {

    private String platformModelId;
    private String modelKey;
    private String modelName;
    private String bizType;
    private String categoryName;
    private String bindingMode;
    private String formRoute;
    /** 当前租户是否已同步同 biz_type 模型 */
    private boolean synced;
    private String syncedModelId;
}
