package com.icoolkj.platform.system.ai.vo;

import lombok.Data;

@Data
public class AiProviderVO {
    private String id;
    private String tenantId;
    private String providerCode;
    private String providerName;
    private String protocol;
    private String baseUrl;
    private boolean apiKeyConfigured;
    private String apiKeyMasked;
    private Integer timeoutSeconds;
    private String status;
    private String remark;
    private boolean platformOwned;
}
