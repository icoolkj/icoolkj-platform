package com.icoolkj.platform.system.ai.vo;

import lombok.Data;

@Data
public class AiModelVO {
    private String id;
    private String tenantId;
    private String providerId;
    private String providerName;
    private String modelCode;
    private String modelName;
    private String capabilities;
    private String defaultParamsJson;
    private String status;
    private Integer sortNo;
    private String remark;
    private boolean platformOwned;
}
