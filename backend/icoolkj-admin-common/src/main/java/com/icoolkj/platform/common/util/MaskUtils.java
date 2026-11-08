package com.icoolkj.platform.common.util;

import org.springframework.util.StringUtils;

/**
 * Lightweight PII masking for display / Excel export (not crypto).
 */
public final class MaskUtils {

    private MaskUtils() {
    }

    /** Keep first 3 and last 4 digits when length ≥ 7; otherwise {@code ****}. */
    public static String phone(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String s = raw.trim();
        if (s.length() < 7) {
            return "****";
        }
        return s.substring(0, 3) + "****" + s.substring(s.length() - 4);
    }

    /** Mask local-part after first char: {@code a***@example.com}. */
    public static String email(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String s = raw.trim();
        int at = s.indexOf('@');
        if (at <= 0 || at == s.length() - 1) {
            return "****";
        }
        String local = s.substring(0, at);
        String domain = s.substring(at);
        if (local.length() == 1) {
            return "*" + domain;
        }
        return local.charAt(0) + "***" + domain;
    }

    /** Keep first character (or first CJK ideograph), mask the rest. */
    public static String name(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String s = raw.trim();
        if (s.length() == 1) {
            return "*";
        }
        return s.charAt(0) + "**";
    }

    /** Mask last octet of IPv4; otherwise return {@code ***.***.***.***}. */
    public static String ip(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String s = raw.trim();
        int lastDot = s.lastIndexOf('.');
        if (lastDot > 0 && lastDot < s.length() - 1 && s.indexOf(':') < 0) {
            return s.substring(0, lastDot + 1) + "***";
        }
        return "***.***.***.***";
    }

    /**
     * PRC ID card: keep first 4 and last 4 when length ≥ 15; otherwise {@code ****}.
     * Spaces/dashes stripped for length check; output uses the stripped form.
     */
    public static String idCard(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String s = raw.trim().replaceAll("[\\s-]", "");
        if (s.length() < 15) {
            return "****";
        }
        return s.substring(0, 4) + "**********" + s.substring(s.length() - 4);
    }

    /**
     * Bank card: keep first 4 and last 4 when length ≥ 10; otherwise {@code ****}.
     * Spaces/dashes stripped for length check; output uses the stripped form.
     */
    public static String bankCard(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String s = raw.trim().replaceAll("[\\s-]", "");
        if (s.length() < 10) {
            return "****";
        }
        return s.substring(0, 4) + "********" + s.substring(s.length() - 4);
    }
}
