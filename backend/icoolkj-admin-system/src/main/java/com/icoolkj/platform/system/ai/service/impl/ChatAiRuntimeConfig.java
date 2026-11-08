package com.icoolkj.platform.system.ai.service.impl;

import com.icoolkj.platform.common.constant.AiCapability;
import com.icoolkj.platform.system.ai.service.AiCapabilityRuntimeService;
import com.icoolkj.platform.system.ai.service.AiRuntimeConfig;
import com.icoolkj.platform.system.ai.support.AiResolvedRuntime;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ChatAiRuntimeConfig implements AiRuntimeConfig {

    private final AiCapabilityRuntimeService capabilityRuntimeService;

    public ChatAiRuntimeConfig(AiCapabilityRuntimeService capabilityRuntimeService) {
        this.capabilityRuntimeService = capabilityRuntimeService;
    }

    private AiResolvedRuntime chat() {
        return capabilityRuntimeService.resolve(AiCapability.CHAT);
    }

    @Override
    public boolean isEnabled() {
        return chat().isEnabled();
    }

    @Override
    public String getBaseUrl() {
        return chat().getBaseUrl();
    }

    @Override
    public String getModel() {
        return chat().getModel();
    }

    @Override
    public String getApiKey() {
        return chat().getApiKey();
    }

    @Override
    public int getTimeoutSeconds() {
        return chat().getTimeoutSeconds();
    }

    @Override
    public String getProviderName() {
        return chat().getProviderName();
    }

    @Override
    public String getProtocol() {
        return chat().getProtocol();
    }

    @Override
    public String getDefaultParamsJson() {
        return chat().getDefaultParamsJson();
    }
}
