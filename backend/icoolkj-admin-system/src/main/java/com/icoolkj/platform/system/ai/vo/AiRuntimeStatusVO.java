package com.icoolkj.platform.system.ai.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiRuntimeStatusVO {

    private String capability;
    private boolean enabled;
    private boolean apiKeyConfigured;
    private String baseUrl;
    private String protocol;
    private Integer timeoutSeconds;
    private String modelCode;
    private String modelName;
    private String providerName;
    private boolean usingPlatformDefault;
}
