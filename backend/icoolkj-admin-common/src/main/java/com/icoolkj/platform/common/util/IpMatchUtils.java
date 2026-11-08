package com.icoolkj.platform.common.util;

import org.springframework.util.StringUtils;

/**
 * IPv4 matching helpers for IP access rules.
 */
public final class IpMatchUtils {

    private IpMatchUtils() {
    }

    public static boolean matches(String clientIp, String ruleAddress, String matchType) {
        if (!StringUtils.hasText(clientIp) || !StringUtils.hasText(ruleAddress) || !StringUtils.hasText(matchType)) {
            return false;
        }
        String normalizedClientIp = clientIp.trim();
        String normalizedRule = ruleAddress.trim();
        return switch (matchType.trim()) {
            case "1" -> normalizedClientIp.equalsIgnoreCase(normalizedRule);
            case "2" -> matchesRange(normalizedClientIp, normalizedRule);
            case "3" -> matchesCidr(normalizedClientIp, normalizedRule);
            default -> false;
        };
    }

    private static boolean matchesRange(String clientIp, String ruleAddress) {
        int separator = ruleAddress.indexOf('-');
        if (separator <= 0 || separator >= ruleAddress.length() - 1) {
            return false;
        }
        Long client = parseIpv4(clientIp);
        Long start = parseIpv4(ruleAddress.substring(0, separator).trim());
        Long end = parseIpv4(ruleAddress.substring(separator + 1).trim());
        if (client == null || start == null || end == null) {
            return false;
        }
        long min = Math.min(start, end);
        long max = Math.max(start, end);
        return client >= min && client <= max;
    }

    private static boolean matchesCidr(String clientIp, String cidr) {
        int slash = cidr.indexOf('/');
        if (slash <= 0) {
            return clientIp.equalsIgnoreCase(cidr);
        }
        Long client = parseIpv4(clientIp);
        Long network = parseIpv4(cidr.substring(0, slash).trim());
        if (client == null || network == null) {
            return false;
        }
        int prefix;
        try {
            prefix = Integer.parseInt(cidr.substring(slash + 1).trim());
        } catch (NumberFormatException ex) {
            return false;
        }
        if (prefix < 0 || prefix > 32) {
            return false;
        }
        long mask = prefix == 0 ? 0L : 0xFFFFFFFFL << (32 - prefix);
        return (client & mask) == (network & mask);
    }

    private static Long parseIpv4(String ip) {
        if (!StringUtils.hasText(ip)) {
            return null;
        }
        String[] parts = ip.trim().split("\\.");
        if (parts.length != 4) {
            return null;
        }
        long value = 0L;
        for (String part : parts) {
            int segment;
            try {
                segment = Integer.parseInt(part);
            } catch (NumberFormatException ex) {
                return null;
            }
            if (segment < 0 || segment > 255) {
                return null;
            }
            value = (value << 8) + segment;
        }
        return value;
    }
}
