package com.icoolkj.platform.framework.tenant;

/**
 * Thread-local holder for {@link TenantContext}.
 * <p>
 * Must call {@link #clear()} after each request to avoid tenant context leakage.
 */
public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(TenantContext context) {
        CONTEXT_HOLDER.set(context);
    }

    public static TenantContext get() {
        return CONTEXT_HOLDER.get();
    }

    public static String getTenantId() {
        TenantContext context = get();
        return context == null ? null : context.getTenantId();
    }

    public static String getTenantCode() {
        TenantContext context = get();
        return context == null ? null : context.getTenantCode();
    }

    public static boolean isPlatform() {
        TenantContext context = get();
        return context != null && context.isPlatform();
    }

    public static boolean isIgnoreTenant() {
        TenantContext context = get();
        return context != null && context.isIgnoreTenant();
    }

    public static boolean hasTenant() {
        return getTenantId() != null;
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
