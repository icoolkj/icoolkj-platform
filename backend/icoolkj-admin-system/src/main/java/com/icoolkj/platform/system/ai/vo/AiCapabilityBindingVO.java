package com.icoolkj.platform.system.ai.vo;

import lombok.Data;

@Data
public class AiCapabilityBindingVO {
    private String id;
    private String tenantId;
    private String capability;
    private String modelId;
    private String modelName;
    private String modelCode;
    private String providerName;
    private Integer enabled;
    private boolean usingPlatformDefault;
}
