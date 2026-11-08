package com.icoolkj.platform.system.iprule.service;

import com.icoolkj.platform.common.enums.ApplyScopeEnum;
import com.icoolkj.platform.framework.cache.CacheService;
import com.icoolkj.platform.system.iprule.domain.IpAccessRuleEntity;

import java.util.List;

/**
 * Cached IP access rules for a tenant.
 */
public interface IpRuleCacheService {

    List<IpAccessRuleEntity> getActiveRules(String tenantId);

    void evict(String tenantId);
}
