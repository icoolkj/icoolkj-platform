package com.icoolkj.platform.infra.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.util.StringUtils;

/** catalog_item.chrome_roles JSON（role → priority int）读写。 */
public final class ChromeRolesJsonHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Integer>> MAP_TYPE = new TypeReference<>() {};

    private ChromeRolesJsonHelper() {
    }

    public static Map<String, Integer> parse(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Integer> map = MAPPER.readValue(json, MAP_TYPE);
            return map != null ? map : Collections.emptyMap();
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    public static String toJson(Map<String, Integer> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        Map<String, Integer> cleaned = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : roles.entrySet()) {
            if (!StringUtils.hasText(e.getKey()) || e.getValue() == null) {
                continue;
            }
            cleaned.put(e.getKey().trim(), e.getValue());
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
