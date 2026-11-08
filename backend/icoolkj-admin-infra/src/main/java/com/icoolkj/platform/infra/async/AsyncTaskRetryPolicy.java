package com.icoolkj.platform.infra.async;

import org.springframework.util.StringUtils;

import java.util.Locale;

public final class AsyncTaskRetryPolicy {

    private AsyncTaskRetryPolicy() {
    }

    public static boolean isRetryable(String errorMessage) {
        if (!StringUtils.hasText(errorMessage)) {
            return false;
        }
        String m = errorMessage.trim();
        if (m.startsWith("error.")) {
            return false;
        }
        String lower = m.toLowerCase(Locale.ROOT);
        return !containsAny(lower,
                "noclassdeffounderror",
                "classnotfoundexception",
                "linkageerror",
                "outofmemoryerror",
                "unsupportedclassversionerror",
                "exceptionininitializererror");
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String n : needles) {
            if (haystack.contains(n)) {
                return true;
            }
        }
        return false;
    }
}
