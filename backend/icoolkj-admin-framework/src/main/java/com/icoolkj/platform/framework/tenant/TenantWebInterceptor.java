package com.icoolkj.platform.framework.tenant;

import com.icoolkj.platform.common.exception.TenantException;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * Binds tenant context per request and clears it on completion.
 */
public class TenantWebInterceptor implements HandlerInterceptor {

    private final TenantProperties tenantProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public TenantWebInterceptor(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!tenantProperties.isEnabled()) {
            TenantContextHolder.set(TenantContext.ignore());
            return true;
        }

        String lookupPath = resolveLookupPath(request);
        if (matchIgnore(lookupPath, tenantProperties.getIgnoreUrls())) {
            TenantContextHolder.set(TenantContext.ignore());
            return true;
        }

        String userTenantId = SecurityContextHelper.getTenantId();
        if (userTenantId != null) {
            TenantContextHolder.set(buildFromAuthenticatedUser(userTenantId, request));
            return true;
        }

        String tenantCode = request.getHeader(tenantProperties.getHeaderTenantCode());
        if (hasText(tenantCode)) {
            TenantContextHolder.set(TenantContext.tenant(null, tenantCode.trim()));
            return true;
        }

        if (tenantProperties.isStrictMode()) {
            throw new TenantException("error.tenant.unresolved");
        }

        TenantContextHolder.set(TenantContext.ignore());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        TenantContextHolder.clear();
    }

    private TenantContext buildFromAuthenticatedUser(String userTenantId, HttpServletRequest request) {
        String tenantCode = request.getHeader(tenantProperties.getHeaderTenantCode());
        String platformTenantId = tenantProperties.getPlatformTenantId();
        boolean platform = hasText(platformTenantId) && platformTenantId.equals(userTenantId);
        return new TenantContext(userTenantId, tenantCode, platform, false);
    }

    private boolean matchIgnore(String requestPath, List<String> ignoreUrls) {
        if (ignoreUrls == null || ignoreUrls.isEmpty()) {
            return false;
        }
        for (String pattern : ignoreUrls) {
            if (pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    /** Servlet path without context-path, aligned with {@code JwtAuthenticationFilter}. */
    private String resolveLookupPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (request.getPathInfo() != null) {
            return servletPath + request.getPathInfo();
        }
        return servletPath;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
