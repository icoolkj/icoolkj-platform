package com.icoolkj.platform.framework.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * No-op when system module does not provide {@link IpAccessGuard}.
 */
@Component
@ConditionalOnMissingBean(IpAccessGuard.class)
public class NoOpIpAccessGuard implements IpAccessGuard {

    @Override
    public void ensureAdminAccessAllowed(String tenantId, String clientIp) {
        // no-op
    }
}
