package com.icoolkj.platform.infra.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.util.StringUtils;

/** 业务名称 *_i18n JSON（locale → text）读写。 */
public final class NameI18nJsonHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};

    private NameI18nJsonHelper() {
    }

    public static Map<String, String> parse(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            Map<String, String> map = MAPPER.readValue(json, MAP_TYPE);
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
        } catch (Exception ex) {
            return null;
        }
    }
}
