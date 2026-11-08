package com.icoolkj.platform.framework.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SensitiveDataMasker {

    private static final String MASK = "******";
    private static final Pattern PHONE = Pattern.compile("(1\\d{2})\\d{4}(\\d{4})");
    private static final Pattern EMAIL = Pattern.compile("([A-Za-z0-9._%+-])[^@\\s]*(@[^@\\s]+)");
    private static final Pattern ID_CARD = Pattern.compile("(\\d{4})\\d{10,13}([0-9Xx]{2})");
    private static final Pattern BANK_CARD = Pattern.compile("(\\d{4})\\d{8,15}(\\d{4})");

    private final ObjectMapper objectMapper;
    private final Set<String> sensitiveFields = new HashSet<>();

    public SensitiveDataMasker(ObjectMapper objectMapper, LoggingProperties properties) {
        this.objectMapper = objectMapper;
        sensitiveFields.addAll(Set.of(
                "password", "passwd", "pwd", "passwordhash",
                "oldpassword", "newpassword", "confirmpassword",
                "token", "accesstoken", "refreshtoken", "authorization",
                "secret", "appsecret", "apikey", "api-key",
                "phone", "mobile", "idcard", "bankcard"
        ));
        if (properties.getSensitiveFields() != null) {
            for (String field : properties.getSensitiveFields()) {
                if (StringUtils.hasText(field)) {
                    sensitiveFields.add(normalize(field));
                }
            }
        }
    }

    public String mask(String raw, int maxLength) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String value = raw.trim();
        String masked = looksLikeJson(value) ? maskJson(value) : maskPlain(value);
        return truncate(masked, maxLength);
    }

    /** Mask sensitive query params such as {@code access_token}. */
    public String maskQueryString(String query) {
        if (!StringUtils.hasText(query)) {
            return query;
        }
        String result = query;
        result = result.replaceAll("(?i)(access_token=)([^&]*)", "$1" + MASK);
        result = result.replaceAll("(?i)(refresh_token=)([^&]*)", "$1" + MASK);
        result = result.replaceAll("(?i)(token=)([^&]*)", "$1" + MASK);
        return result;
    }

    public String maskObject(Object value, int maxLength) {
        if (value == null) {
            return null;
        }
        try {
            return mask(objectMapper.writeValueAsString(value), maxLength);
        } catch (JsonProcessingException ex) {
            return mask(String.valueOf(value), maxLength);
        }
    }

    private String maskJson(String value) {
        try {
            JsonNode root = objectMapper.readTree(value);
            maskNode(root);
            return objectMapper.writeValueAsString(root);
        } catch (Exception ex) {
            return maskPlain(value);
        }
    }

    private void maskNode(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (isSensitiveField(entry.getKey())) {
                    objectNode.put(entry.getKey(), MASK);
                } else {
                    maskNode(entry.getValue());
                }
            }
        } else if (node instanceof ArrayNode arrayNode) {
            for (JsonNode item : arrayNode) {
                maskNode(item);
            }
        }
    }

    private String maskPlain(String value) {
        String result = value;
        result = result.replaceAll("(?i)(access_token\\s*[=:]\\s*)([^&\\s,}]+)", "$1" + MASK);
        result = result.replaceAll("(?i)(refresh_token\\s*[=:]\\s*)([^&\\s,}]+)", "$1" + MASK);
        for (String field : sensitiveFields) {
            result = result.replaceAll("(?i)(" + Pattern.quote(field) + "\\s*[=:]\\s*)([^&\\s,}]+)", "$1" + MASK);
        }
        result = PHONE.matcher(result).replaceAll("$1****$2");
        result = EMAIL.matcher(result).replaceAll("$1***$2");
        result = ID_CARD.matcher(result).replaceAll("$1**********$2");
        result = BANK_CARD.matcher(result).replaceAll("$1********$2");
        return result;
    }

    private boolean isSensitiveField(String fieldName) {
        String normalized = normalize(fieldName);
        if (sensitiveFields.contains(normalized)) {
            return true;
        }
        // oldPassword / newPassword / passwordCipher …
        return normalized.contains("password") || normalized.contains("passwd");
    }

    private String normalize(String fieldName) {
        return fieldName == null ? "" : fieldName.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
    }

    private boolean looksLikeJson(String value) {
        return (value.startsWith("{") && value.endsWith("}")) || (value.startsWith("[") && value.endsWith("]"));
    }

    private String truncate(String value, int maxLength) {
        if (value == null || maxLength <= 0 || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
