package com.icoolkj.platform.system.ai.support;

import com.icoolkj.platform.system.ai.service.AiRuntimeConfig;

public final class AiResolvedRuntime implements AiRuntimeConfig {

    private final boolean enabled;
    private final String baseUrl;
    private final String model;
    private final String apiKey;
    private final int timeoutSeconds;
    private final String providerId;
    private final String providerName;
    private final String protocol;
    private final String modelId;
    private final String capability;
    private final String defaultParamsJson;

    public AiResolvedRuntime(
            boolean enabled,
            String baseUrl,
            String model,
            String apiKey,
            int timeoutSeconds,
            String providerId,
            String providerName,
            String protocol,
            String modelId,
            String capability,
            String defaultParamsJson) {
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.model = model;
        this.apiKey = apiKey;
        this.timeoutSeconds = timeoutSeconds;
        this.providerId = providerId;
        this.providerName = providerName;
        this.protocol = protocol;
        this.modelId = modelId;
        this.capability = capability;
        this.defaultParamsJson = defaultParamsJson;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getDefaultParamsJson() {
        return defaultParamsJson;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getModelId() {
        return modelId;
    }

    public String getCapability() {
        return capability;
    }
}
