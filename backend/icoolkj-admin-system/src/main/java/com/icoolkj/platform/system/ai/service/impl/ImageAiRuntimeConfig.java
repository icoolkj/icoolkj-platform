package com.icoolkj.platform.system.ai.service.impl;

import com.icoolkj.platform.common.constant.AiCapability;
import com.icoolkj.platform.system.ai.service.AiCapabilityRuntimeService;
import com.icoolkj.platform.system.ai.service.AiRuntimeConfig;
import com.icoolkj.platform.system.ai.support.AiResolvedRuntime;
import org.springframework.stereotype.Component;

@Component
public class ImageAiRuntimeConfig implements AiRuntimeConfig {

    private final AiCapabilityRuntimeService capabilityRuntimeService;

    public ImageAiRuntimeConfig(AiCapabilityRuntimeService capabilityRuntimeService) {
        this.capabilityRuntimeService = capabilityRuntimeService;
    }

    private AiResolvedRuntime image() {
        return capabilityRuntimeService.resolve(AiCapability.IMAGE);
    }

    @Override
    public boolean isEnabled() {
        return image().isEnabled();
    }

    @Override
    public String getBaseUrl() {
        return image().getBaseUrl();
    }

    @Override
    public String getModel() {
        return image().getModel();
    }

    @Override
    public String getApiKey() {
        return image().getApiKey();
    }

    @Override
    public int getTimeoutSeconds() {
        return image().getTimeoutSeconds();
    }

    @Override
    public String getProviderName() {
        return image().getProviderName();
    }

    @Override
    public String getProtocol() {
        return image().getProtocol();
    }

    @Override
    public String getDefaultParamsJson() {
        return image().getDefaultParamsJson();
    }
}
