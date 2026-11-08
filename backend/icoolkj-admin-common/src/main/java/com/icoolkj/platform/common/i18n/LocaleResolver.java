package com.icoolkj.platform.common.i18n;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * Parses {@code Accept-Language} and normalizes to supported platform locales.
 */
public final class LocaleResolver {

    public static final String LOCALE_ZH_CN = "zh-CN";
    public static final String LOCALE_EN_US = "en-US";
    public static final List<String> SUPPORTED = List.of(LOCALE_ZH_CN, LOCALE_EN_US);

    private LocaleResolver() {
    }

    public static String resolve(String acceptLanguage) {
        return resolve(acceptLanguage, LOCALE_ZH_CN, SUPPORTED);
    }

    public static String resolve(String acceptLanguage, String defaultLocale) {
        return resolve(acceptLanguage, defaultLocale, SUPPORTED);
    }

    public static String resolve(String acceptLanguage, String defaultLocale, List<String> supportedLocales) {
        List<String> supported = CollectionUtils.isEmpty(supportedLocales) ? SUPPORTED : supportedLocales;
        if (!StringUtils.hasText(acceptLanguage)) {
            return normalize(defaultLocale, supported);
        }
        String[] parts = acceptLanguage.split(",");
        for (String part : parts) {
            String tag = part.split(";")[0].trim();
            if (!StringUtils.hasText(tag)) {
                continue;
            }
            String normalized = normalize(tag, supported);
            if (supported.contains(normalized)) {
                return normalized;
            }
            if (tag.toLowerCase(Locale.ROOT).startsWith("en") && supported.contains(LOCALE_EN_US)) {
                return LOCALE_EN_US;
            }
            if (tag.toLowerCase(Locale.ROOT).startsWith("zh") && supported.contains(LOCALE_ZH_CN)) {
                return LOCALE_ZH_CN;
            }
        }
        return normalize(defaultLocale, supported);
    }

    public static String normalize(String locale) {
        return normalize(locale, SUPPORTED);
    }

    public static String normalize(String locale, List<String> supportedLocales) {
        List<String> supported = CollectionUtils.isEmpty(supportedLocales) ? SUPPORTED : supportedLocales;
        if (!StringUtils.hasText(locale)) {
            return supported.contains(LOCALE_ZH_CN) ? LOCALE_ZH_CN : supported.get(0);
        }
        String value = locale.trim();
        if (supported.contains(value)) {
            return value;
        }
        if (LOCALE_EN_US.equalsIgnoreCase(value) || "en".equalsIgnoreCase(value)) {
            return supported.contains(LOCALE_EN_US) ? LOCALE_EN_US : supported.get(0);
        }
        if (LOCALE_ZH_CN.equalsIgnoreCase(value) || "zh".equalsIgnoreCase(value)) {
            return supported.contains(LOCALE_ZH_CN) ? LOCALE_ZH_CN : supported.get(0);
        }
        return supported.contains(LOCALE_ZH_CN) ? LOCALE_ZH_CN : supported.get(0);
    }
}
