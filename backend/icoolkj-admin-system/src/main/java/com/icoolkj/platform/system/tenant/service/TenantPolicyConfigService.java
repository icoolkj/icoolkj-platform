package com.icoolkj.platform.system.tenant.service;

import com.icoolkj.platform.system.tenant.dto.TenantPolicyConfigUpdateRequest;
import com.icoolkj.platform.system.tenant.vo.TenantPolicyConfigItemVO;

import java.util.List;

/**
 * Tenant self-service overrides for {@code ConfigKeyRegistry} tenant-overridable keys.
 */
public interface TenantPolicyConfigService {

    List<TenantPolicyConfigItemVO> catalog();

    void upsert(String configKey, TenantPolicyConfigUpdateRequest request);

    void reset(String configKey);

    void refreshCache();
}
