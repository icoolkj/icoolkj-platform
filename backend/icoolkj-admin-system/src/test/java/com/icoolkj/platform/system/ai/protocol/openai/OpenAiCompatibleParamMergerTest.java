package com.icoolkj.platform.system.ai.protocol.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiCompatibleParamMergerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void mergesWhitelistedChatParams() throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", "m1");
        body.put("stream", false);
        OpenAiCompatibleParamMerger.mergeChatDefaults(
                mapper, body, "{\"temperature\":0.2,\"max_tokens\":128,\"foo\":1}");
        assertEquals(0.2d, body.get("temperature").asDouble(), 0.0001);
        assertEquals(128, body.get("max_tokens").asInt());
        assertFalse(body.has("foo"));
        assertEquals("m1", body.get("model").asText());
    }

    @Test
    void doesNotOverrideExistingBodyFields() {
        ObjectNode body = mapper.createObjectNode();
        body.put("temperature", 0.9);
        OpenAiCompatibleParamMerger.mergeChatDefaults(
                mapper, body, "{\"temperature\":0.1}");
        assertEquals(0.9d, body.get("temperature").asDouble(), 0.0001);
    }

    @Test
    void ignoresInvalidJson() {
        ObjectNode body = mapper.createObjectNode();
        OpenAiCompatibleParamMerger.mergeChatDefaults(mapper, body, "{not-json");
        assertTrue(body.isEmpty());
    }
}
