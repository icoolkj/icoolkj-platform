package com.icoolkj.platform.system.notice.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoticeContentUtilsTest {

    @Test
    void sanitize_removesScriptAndAccessToken() {
        String raw = "<p>hello</p><script>alert(1)</script>"
                + "<img src=\"/api/infra/file/1234567890123456789/download?inline=1&access_token=abc\" />";
        String cleaned = NoticeContentUtils.sanitize(raw);
        assertTrue(cleaned.contains("hello"));
        assertFalse(cleaned.contains("script"));
        assertFalse(cleaned.contains("access_token"));
        assertTrue(cleaned.contains("/infra/file/1234567890123456789/download?inline=1"));
    }

    @Test
    void sanitize_stripsBlobAndEventHandlers() {
        String raw = "<p><img src=\"blob:http://localhost/1\" onclick=\"alert(1)\" data-infra-canonical-src=\"x\" /></p>";
        String cleaned = NoticeContentUtils.sanitize(raw);
        assertFalse(cleaned.contains("blob:"));
        assertFalse(cleaned.contains("onclick"));
        assertFalse(cleaned.contains("data-infra-canonical-src"));
    }
}
