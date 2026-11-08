package com.icoolkj.platform.framework.logging;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.UUID;

public final class LogContext {

    public static final String TRACE_ID = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private LogContext() {
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    public static String ensureTraceId(String candidate) {
        String traceId = StringUtils.hasText(candidate) ? candidate.trim() : getTraceId();
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(TRACE_ID, traceId);
        return traceId;
    }

    public static void clear() {
        MDC.remove(TRACE_ID);
    }
}
