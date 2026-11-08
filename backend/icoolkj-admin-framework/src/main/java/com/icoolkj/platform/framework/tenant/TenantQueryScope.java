package com.icoolkj.platform.framework.tenant;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageQuery;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Enforces tenant boundaries on list/detail queries.
 * <p>
 * Business tenants always see only their own {@code tenant_id}. Platform operators ({@code tenant_id=0})
 * may list across tenants on ignore-table modules when no filter is set.
 */
public final class TenantQueryScope {

    private TenantQueryScope() {
    }

    /**
     * Binds {@link PageQuery#getTenantId()} for business tenants; strips cross-tenant client overrides.
     */
    public static void applyPageQuery(PageQuery query) {
        if (query == null) {
            return;
        }
        if (TenantContextHolder.isPlatform()) {
            return;
        }
        query.setTenantId(requiredOperatorTenantId());
    }

    /**
     * Ensures the loaded row belongs to the current business tenant.
     */
    public static void assertReadable(String entityTenantId) {
        if (TenantContextHolder.isPlatform()) {
            return;
        }
        String operatorTenantId = requiredOperatorTenantId();
        if (!operatorTenantId.equals(entityTenantId)) {
            throw new BusinessException("error.forbidden");
        }
    }

    public static String requiredOperatorTenantId() {
        String tenantId = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            tenantId = TenantContextHolder.getTenantId();
        }
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException("error.forbidden");
        }
        return tenantId;
    }

    public static boolean isPlatformOperator() {
        return TenantContextHolder.isPlatform();
    }

    /** Binds {@code tenant_id} for custom page DTOs that are not {@link PageQuery} subclasses. */
    public static void bindQueryTenantId(Consumer<String> setter) {
        if (setter == null || TenantContextHolder.isPlatform()) {
            return;
        }
        setter.accept(requiredOperatorTenantId());
    }

    /**
     * Runs page queries with an explicit {@code tenantId} under platform operator without tenant-line drift.
     */
    public static <T> T runForExplicitTenant(PageQuery query, Supplier<T> supplier) {
        if (query != null && isPlatformOperator() && StringUtils.hasText(query.getTenantId())) {
            return TenantScopeRunner.runIgnoringTenant(supplier);
        }
        return supplier.get();
    }
}
