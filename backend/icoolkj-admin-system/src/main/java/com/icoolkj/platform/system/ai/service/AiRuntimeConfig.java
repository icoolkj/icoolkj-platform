package com.icoolkj.platform.system.ai.service;

public interface AiRuntimeConfig {

    boolean isEnabled();

    String getBaseUrl();

    String getModel();

    String getApiKey();

    int getTimeoutSeconds();

    String getProviderName();

    String getProtocol();

    String getDefaultParamsJson();
}
