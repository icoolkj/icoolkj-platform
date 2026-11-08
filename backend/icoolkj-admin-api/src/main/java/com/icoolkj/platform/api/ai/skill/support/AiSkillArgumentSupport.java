package com.icoolkj.platform.api.ai.skill.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

/**
 * Shared parsing helpers for AI skill tool arguments.
 */
public final class AiSkillArgumentSupport {

    public static final int DEFAULT_LIMIT = 10;
    public static final int MAX_LIMIT = 20;

    private AiSkillArgumentSupport() {
    }

    public static int parseLimit(ObjectMapper objectMapper, String argumentsJson) {
        try {
            if (!StringUtils.hasText(argumentsJson)) {
                return DEFAULT_LIMIT;
            }
            JsonNode node = objectMapper.readTree(argumentsJson);
            int limit = node.path("limit").asInt(DEFAULT_LIMIT);
            if (limit < 1) {
                return DEFAULT_LIMIT;
            }
            return Math.min(limit, MAX_LIMIT);
        } catch (Exception ex) {
            return DEFAULT_LIMIT;
        }
    }

    public static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
