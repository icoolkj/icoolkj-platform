package com.icoolkj.platform.framework.security.xss;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.web.util.HtmlUtils;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/** Escapes query/form parameter values to reduce reflected XSS via request params. */
final class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return clean(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] cleaned = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleaned[i] = clean(values[i]);
        }
        return cleaned;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> original = super.getParameterMap();
        if (original == null || original.isEmpty()) {
            return original;
        }
        Map<String, String[]> cleaned = new LinkedHashMap<>(original.size());
        for (Map.Entry<String, String[]> entry : original.entrySet()) {
            String[] values = entry.getValue();
            if (values == null) {
                cleaned.put(entry.getKey(), null);
                continue;
            }
            String[] copy = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                copy[i] = clean(values[i]);
            }
            cleaned.put(entry.getKey(), copy);
        }
        return Collections.unmodifiableMap(cleaned);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return super.getParameterNames();
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(value);
    }
}
