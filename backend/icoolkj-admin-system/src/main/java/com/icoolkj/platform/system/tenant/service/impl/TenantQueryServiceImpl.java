package com.icoolkj.platform.system.tenant.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.common.exception.UnauthorizedException;
import com.icoolkj.platform.framework.tenant.TenantProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.icoolkj.platform.system.tenant.domain.TenantEntity;
import com.icoolkj.platform.system.tenant.mapper.TenantMapper;
import com.icoolkj.platform.system.tenant.service.TenantQueryService;

import java.time.LocalDateTime;

@Service
public class TenantQueryServiceImpl implements TenantQueryService {

    private final TenantMapper tenantMapper;
    private final TenantProperties tenantProperties;

    public TenantQueryServiceImpl(TenantMapper tenantMapper, TenantProperties tenantProperties) {
        this.tenantMapper = tenantMapper;
        this.tenantProperties = tenantProperties;
    }

    @Override
    public String resolveLoginTenantId(String tenantCode) {
        if (!StringUtils.hasText(tenantCode)) {
            throw new UnauthorizedException("validation.tenant_code.required");
        }

        String normalizedCode = tenantCode.trim();
        if (isPlatformTenantCode(normalizedCode)) {
            return tenantProperties.getPlatformTenantId();
        }

        TenantEntity tenant = tenantMapper.selectByTenantCode(normalizedCode);
        if (tenant == null) {
            throw new UnauthorizedException("error.tenant.not_found_or_disabled");
        }
        if (!StatusEnum.isNormal(tenant.getStatus())) {
            throw new UnauthorizedException("error.tenant.not_found_or_disabled");
        }
        if (tenant.getExpireAt() != null && tenant.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("error.tenant.expired");
        }
        return tenant.getId();
    }

    private boolean isPlatformTenantCode(String tenantCode) {
        if (TenantConstants.PLATFORM_TENANT_ID.equals(tenantCode)) {
            return true;
        }
        String configuredCode = tenantProperties.getPlatformTenantCode();
        return StringUtils.hasText(configuredCode) && configuredCode.equalsIgnoreCase(tenantCode);
    }
}
