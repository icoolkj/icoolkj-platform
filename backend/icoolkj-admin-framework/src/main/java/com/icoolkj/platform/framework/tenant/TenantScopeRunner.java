package com.icoolkj.platform.framework.tenant;

import java.util.function.Supplier;

/**
 * Runs database work without tenant-line filtering.
 */
public final class TenantScopeRunner {

    private TenantScopeRunner() {
    }

    public static <T> T runIgnoringTenant(Supplier<T> supplier) {
        TenantContext previous = TenantContextHolder.get();
        boolean previousIgnore = previous != null && previous.isIgnoreTenant();
        try {
            if (previous == null) {
                TenantContextHolder.set(TenantContext.ignore());
            } else {
                previous.setIgnoreTenant(true);
            }
            return supplier.get();
        } finally {
            if (previous == null) {
                TenantContextHolder.clear();
            } else {
                previous.setIgnoreTenant(previousIgnore);
            }
        }
    }

    public static void runIgnoringTenant(Runnable runnable) {
        runIgnoringTenant(() -> {
            runnable.run();
            return null;
        });
    }
}
