package com.icoolkj.platform.system.ai.protocol.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.StringUtils;

import java.util.Set;

public final class OpenAiCompatibleParamMerger {

    public static final Set<String> CHAT_KEYS = Set.of(
            "temperature",
            "top_p",
            "max_tokens",
            "presence_penalty",
            "frequency_penalty",
            "stop",
            "seed",
            "n",
            "response_format",
            "logit_bias",
            "user"
    );

    private OpenAiCompatibleParamMerger() {
    }

    public static void mergeChatDefaults(ObjectMapper mapper, ObjectNode body, String defaultParamsJson) {
        merge(mapper, body, defaultParamsJson, CHAT_KEYS, Set.of("model", "messages", "stream", "tools", "tool_choice"));
    }

    private static void merge(
            ObjectMapper mapper,
            ObjectNode body,
            String defaultParamsJson,
            Set<String> allowKeys,
            Set<String> reservedKeys) {
        if (body == null || !StringUtils.hasText(defaultParamsJson)) {
            return;
        }
        try {
            JsonNode root = mapper.readTree(defaultParamsJson.trim());
            if (root == null || !root.isObject()) {
                return;
            }
            root.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                if (!StringUtils.hasText(key) || reservedKeys.contains(key) || !allowKeys.contains(key)) {
                    return;
                }
                if (body.has(key)) {
                    return;
                }
                body.set(key, entry.getValue().deepCopy());
            });
        } catch (Exception ignored) {
        }
    }
}
