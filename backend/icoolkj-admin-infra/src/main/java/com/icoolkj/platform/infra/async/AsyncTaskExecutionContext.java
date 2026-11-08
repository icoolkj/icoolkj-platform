package com.icoolkj.platform.infra.async;

import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.tenant.TenantContext;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Tenant + security + locale context helpers for async task execution.
 * Prefer {@link AsyncTaskPrincipalResolver} on the worker; minimal LoginUser is fallback only.
 */
public final class AsyncTaskExecutionContext {

    private AsyncTaskExecutionContext() {
    }

    public static TenantContext tenantFrom(AsyncTaskMessage message, AsyncTaskEntity entity) {
        String tenantId = tenantIdFrom(message, entity);
        if (!StringUtils.hasText(tenantId)) {
            return null;
        }
        return TenantContext.tenant(tenantId, null);
    }

    public static String userIdFrom(AsyncTaskMessage message, AsyncTaskEntity entity) {
        return firstText(
                message == null ? null : message.userId(),
                entity == null ? null : entity.getUserId());
    }

    public static String tenantIdFrom(AsyncTaskMessage message, AsyncTaskEntity entity) {
        return firstText(
                message == null ? null : message.tenantId(),
                entity == null ? null : entity.getTenantId());
    }

    public static String localeFrom(AsyncTaskMessage message, AsyncTaskEntity entity) {
        return firstText(
                message == null ? null : message.locale(),
                entity == null ? null : entity.getLocale());
    }

    /** Capture current request locale for persistence on submit. */
    public static String captureLocaleTag() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale == null) {
            return LocaleResolver.LOCALE_ZH_CN;
        }
        return LocaleResolver.normalize(locale.toLanguageTag());
    }

    public static Locale toLocale(String localeTag) {
        if (!StringUtils.hasText(localeTag)) {
            return null;
        }
        return Locale.forLanguageTag(LocaleResolver.normalize(localeTag.trim()));
    }

    /** Fallback when no {@link AsyncTaskPrincipalResolver} bean is available (unit tests). */
    public static Authentication minimalAuthentication(String tenantId, String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        LoginUser login = new LoginUser();
        login.setUserId(userId.trim());
        login.setTenantId(StringUtils.hasText(tenantId) ? tenantId.trim() : null);
        login.setUsername("async-worker");
        login.setPermissions(Collections.emptyList());
        return new UsernamePasswordAuthenticationToken(login, null, login.getAuthorities());
    }

    /** @deprecated use {@link #minimalAuthentication} or {@link AsyncTaskPrincipalResolver} */
    @Deprecated
    public static Authentication authenticationFrom(AsyncTaskMessage message, AsyncTaskEntity entity) {
        return minimalAuthentication(tenantIdFrom(message, entity), userIdFrom(message, entity));
    }

    public static <T> T runWith(Authentication authentication, TenantContext tenantContext, Supplier<T> action) {
        return runWith(authentication, tenantContext, (Locale) null, action);
    }

    public static <T> T runWith(Authentication authentication,
                                TenantContext tenantContext,
                                Locale locale,
                                Supplier<T> action) {
        SecurityContext previousSecurity = SecurityContextHolder.getContext();
        TenantContext previousTenant = TenantContextHolder.get();
        Locale previousLocale = LocaleContextHolder.getLocale();
        try {
            if (authentication != null) {
                SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                ctx.setAuthentication(authentication);
                SecurityContextHolder.setContext(ctx);
            } else {
                SecurityContextHolder.clearContext();
            }
            if (tenantContext != null) {
                TenantContextHolder.set(tenantContext);
            } else {
                TenantContextHolder.clear();
            }
            if (locale != null) {
                LocaleContextHolder.setLocale(locale);
            }
            return action.get();
        } finally {
            SecurityContextHolder.setContext(previousSecurity);
            if (previousTenant != null) {
                TenantContextHolder.set(previousTenant);
            } else {
                TenantContextHolder.clear();
            }
            if (locale != null) {
                LocaleContextHolder.setLocale(previousLocale);
            }
        }
    }

    private static String firstText(String a, String b) {
        if (StringUtils.hasText(a)) {
            return a.trim();
        }
        if (StringUtils.hasText(b)) {
            return b.trim();
        }
        return null;
    }
}
