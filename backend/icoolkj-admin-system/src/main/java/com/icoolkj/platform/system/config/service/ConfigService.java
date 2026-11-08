package com.icoolkj.platform.system.config.service;

/**
 * Resolves tenant-scoped configuration with platform fallback.
 */
public interface ConfigService {

    String getString(String tenantId, String configKey, String defaultValue);

    int getInt(String tenantId, String configKey, int defaultValue);

    boolean getBoolean(String tenantId, String configKey, boolean defaultValue);

    void evict(String tenantId, String configKey);
}
