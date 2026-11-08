package com.icoolkj.platform.system.config.service;

import com.icoolkj.platform.framework.cache.CacheService;

/**
 * Redis cache for resolved tenant configuration.
 */
public interface ConfigCacheService {

    String get(String tenantId, String configKey);

    void put(String tenantId, String configKey, String value);

    void evict(String tenantId, String configKey);

    /** Clears all config cache entries for one tenant. */
    void refreshTenant(String tenantId);

    void refreshAll();
}
