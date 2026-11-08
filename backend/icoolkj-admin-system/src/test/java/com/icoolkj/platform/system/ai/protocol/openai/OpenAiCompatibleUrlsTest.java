package com.icoolkj.platform.system.ai.protocol.openai;

import com.icoolkj.platform.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenAiCompatibleUrlsTest {

    @Test
    void acceptsApiRoot() {
        assertEquals(
                "http://127.0.0.1:8317/v1",
                OpenAiCompatibleUrls.requireApiRoot("http://127.0.0.1:8317/v1/"));
    }

    @Test
    void rejectsResourceEndpointAsBaseUrl() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> OpenAiCompatibleUrls.requireApiRoot(
                        "http://127.0.0.1:8317/v1" + OpenAiCompatibleEndpoints.IMAGES_GENERATIONS));
        assertEquals("error.ai.base_url_must_be_api_root", ex.getMessage());
    }

    @Test
    void rejectsChatCompletionsAsBaseUrl() {
        assertThrows(
                BusinessException.class,
                () -> OpenAiCompatibleUrls.requireApiRoot(
                        "https://api.example.com/v1" + OpenAiCompatibleEndpoints.CHAT_COMPLETIONS));
    }

    @Test
    void buildsAbsoluteUrlFromEndpoints() {
        assertEquals(
                "http://127.0.0.1:8317/v1/chat/completions",
                OpenAiCompatibleEndpoints.absoluteUrl(
                        "http://127.0.0.1:8317/v1",
                        OpenAiCompatibleEndpoints.CHAT_COMPLETIONS));
    }

    @Test
    void rejectsBlank() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> OpenAiCompatibleUrls.requireApiRoot("  "));
        assertEquals("error.ai.base_url_required", ex.getMessage());
    }
}
