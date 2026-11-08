package com.icoolkj.platform.common.util;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.context.i18n.LocaleContextHolder;

/** 按当前请求 Locale 拼接展示用列表（避免硬编码中文顿号）。 */
public final class LocaleListJoin {

    private LocaleListJoin() {
    }

    public static String separator() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && "zh".equalsIgnoreCase(locale.getLanguage())) {
            return "、";
        }
        return ", ";
    }

    public static String join(Collection<String> parts) {
        if (parts == null || parts.isEmpty()) {
            return "";
        }
        String sep = separator();
        return parts.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(sep));
    }
}
