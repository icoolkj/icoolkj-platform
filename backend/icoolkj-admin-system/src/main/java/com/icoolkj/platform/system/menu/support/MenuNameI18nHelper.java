package com.icoolkj.platform.system.menu.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helpers for {@code sys_menu.name_i18n} JSON map {@code locale -> label}.
 * Resolve order: requested locale → zh-CN → {@code menuName} fallback.
 */
public final class MenuNameI18nHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
    };

    private MenuNameI18nHelper() {
    }

    public static Map<String, String> parse(String nameI18nJson) {
        if (!StringUtils.hasText(nameI18nJson)) {
            return Collections.emptyMap();
        }
        try {
            Map<String, String> map = MAPPER.readValue(nameI18nJson, MAP_TYPE);
            return map != null ? map : Collections.emptyMap();
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    public static String toJson(Map<String, String> nameI18n) {
        if (nameI18n == null || nameI18n.isEmpty()) {
            return null;
        }
        Map<String, String> cleaned = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : nameI18n.entrySet()) {
            if (StringUtils.hasText(e.getKey()) && StringUtils.hasText(e.getValue())) {
                cleaned.put(e.getKey().trim(), e.getValue().trim());
            }
        }
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(cleaned);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid nameI18n", e);
        }
    }

    /**
     * Build JSON from form map; ensure zh-CN from {@code menuName} when missing.
     */
    public static String buildJson(Map<String, String> nameI18n, String menuName) {
        Map<String, String> map = new LinkedHashMap<>();
        if (nameI18n != null) {
            for (Map.Entry<String, String> e : nameI18n.entrySet()) {
                if (StringUtils.hasText(e.getKey()) && StringUtils.hasText(e.getValue())) {
                    map.put(e.getKey().trim(), e.getValue().trim());
                }
            }
        }
        if (StringUtils.hasText(menuName) && !map.containsKey(LocaleResolver.LOCALE_ZH_CN)) {
            map.put(LocaleResolver.LOCALE_ZH_CN, menuName.trim());
        }
        return toJson(map);
    }

    public static String resolve(String locale, String nameI18nJson, String menuName) {
        Map<String, String> map = parse(nameI18nJson);
        String normalized = LocaleResolver.normalize(locale);
        String value = map.get(normalized);
        if (StringUtils.hasText(value)) {
            return value;
        }
        if (!LocaleResolver.LOCALE_ZH_CN.equals(normalized)) {
            value = map.get(LocaleResolver.LOCALE_ZH_CN);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return StringUtils.hasText(menuName) ? menuName : "";
    }
}
