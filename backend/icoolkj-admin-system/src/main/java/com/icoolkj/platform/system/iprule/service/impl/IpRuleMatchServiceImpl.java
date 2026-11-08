package com.icoolkj.platform.system.iprule.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.ApplyScopeEnum;
import com.icoolkj.platform.common.enums.IpRuleTypeEnum;
import com.icoolkj.platform.common.exception.IpAccessDeniedException;
import com.icoolkj.platform.common.util.IpMatchUtils;
import com.icoolkj.platform.framework.security.IpAccessGuard;
import com.icoolkj.platform.system.iprule.domain.IpAccessRuleEntity;
import com.icoolkj.platform.system.iprule.service.IpRuleCacheService;
import com.icoolkj.platform.system.iprule.service.IpRuleMatchService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import com.icoolkj.platform.system.auth.service.impl.LoginSecurityConfigService;

@Service
public class IpRuleMatchServiceImpl implements IpRuleMatchService, IpAccessGuard {

    private static final String IP_LOGIN_DENIED = "error.ip.login_denied";
    private static final String IP_ACCESS_DENIED = "error.ip.access_denied";

    private final IpRuleCacheService ipRuleCacheService;
    private final LoginSecurityConfigService loginSecurityConfigService;

    public IpRuleMatchServiceImpl(IpRuleCacheService ipRuleCacheService,
                                  LoginSecurityConfigService loginSecurityConfigService) {
        this.ipRuleCacheService = ipRuleCacheService;
        this.loginSecurityConfigService = loginSecurityConfigService;
    }

    @Override
    public void ensureLoginAllowed(String tenantId, String clientIp) {
        ensureAllowed(tenantId, clientIp, this::isLoginScope, IP_LOGIN_DENIED);
    }

    @Override
    public void ensureAdminAccessAllowed(String tenantId, String clientIp) {
        ensureAllowed(tenantId, clientIp, this::isAdminScope, IP_ACCESS_DENIED);
    }

    private void ensureAllowed(String tenantId,
                               String clientIp,
                               Predicate<String> scopeMatcher,
                               String denyMessage) {
        if (!StringUtils.hasText(clientIp)) {
            String defaultPolicy = loginSecurityConfigService.getIpDefaultPolicy(tenantId);
            if ("deny".equalsIgnoreCase(defaultPolicy)) {
                throw new IpAccessDeniedException(denyMessage);
            }
            return;
        }

        List<IpAccessRuleEntity> rules = ipRuleCacheService.getActiveRules(tenantId);
        List<IpAccessRuleEntity> matched = rules.stream()
                .filter(rule -> scopeMatcher.test(rule.getApplyScope()))
                .filter(rule -> IpMatchUtils.matches(clientIp, rule.getIpAddress(), rule.getMatchType()))
                .toList();

        if (matched.stream().anyMatch(this::isPlatformDenyRule)) {
            throw new IpAccessDeniedException(denyMessage);
        }

        IpAccessRuleEntity topRule = matched.stream()
                .max(Comparator.comparingInt(rule -> rule.getPriorityNo() == null ? 0 : rule.getPriorityNo()))
                .orElse(null);

        if (topRule != null) {
            if (IpRuleTypeEnum.DENY.getCode().equals(topRule.getRuleType())) {
                throw new IpAccessDeniedException(denyMessage);
            }
            return;
        }

        String defaultPolicy = loginSecurityConfigService.getIpDefaultPolicy(tenantId);
        if ("deny".equalsIgnoreCase(defaultPolicy)) {
            throw new IpAccessDeniedException(denyMessage);
        }
    }

    private boolean isLoginScope(String applyScope) {
        if (!StringUtils.hasText(applyScope)) {
            return false;
        }
        return ApplyScopeEnum.LOGIN.getCode().equals(applyScope)
                || ApplyScopeEnum.ALL.getCode().equals(applyScope);
    }

    private boolean isAdminScope(String applyScope) {
        if (!StringUtils.hasText(applyScope)) {
            return false;
        }
        return ApplyScopeEnum.ADMIN.getCode().equals(applyScope)
                || ApplyScopeEnum.ALL.getCode().equals(applyScope);
    }

    private boolean isPlatformDenyRule(IpAccessRuleEntity rule) {
        return TenantConstants.PLATFORM_TENANT_ID.equals(rule.getTenantId())
                && IpRuleTypeEnum.DENY.getCode().equals(rule.getRuleType());
    }
}
