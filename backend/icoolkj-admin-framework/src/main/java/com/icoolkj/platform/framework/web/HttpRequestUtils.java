package com.icoolkj.platform.framework.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * HTTP request helpers for web runtime.
 */
public final class HttpRequestUtils {

    private static final String UNKNOWN = "unknown";

    private HttpRequestUtils() {
    }

    public static HttpServletRequest getCurrentRequest() {
        HttpServletRequest current = CurrentRequestHolder.get();
        if (current != null) {
            return current;
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    public static String getClientIp() {
        return getClientIp(getCurrentRequest());
    }

    public static String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader("User-Agent");
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (isValidIp(ip)) {
                return firstIp(ip);
            }
        }
        return request.getRemoteAddr();
    }

    private static String firstIp(String ipChain) {
        int index = ipChain.indexOf(',');
        if (index > 0) {
            return ipChain.substring(0, index).trim();
        }
        return ipChain.trim();
    }

    private static boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip.trim());
    }
}
