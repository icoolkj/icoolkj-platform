package com.icoolkj.platform.framework.web;

import jakarta.servlet.http.HttpServletRequest;

public final class CurrentRequestHolder {

    private static final ThreadLocal<HttpServletRequest> HOLDER = new ThreadLocal<>();

    private CurrentRequestHolder() {
    }

    public static void set(HttpServletRequest request) {
        HOLDER.set(request);
    }

    public static HttpServletRequest get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
