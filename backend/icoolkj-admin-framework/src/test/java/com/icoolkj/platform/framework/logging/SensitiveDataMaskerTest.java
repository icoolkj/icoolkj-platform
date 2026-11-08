package com.icoolkj.platform.framework.logging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SensitiveDataMaskerTest {

    private SensitiveDataMasker masker;

    @BeforeEach
    void setUp() {
        LoggingProperties properties = new LoggingProperties();
        properties.getSensitiveFields().add("password");
        masker = new SensitiveDataMasker(new ObjectMapper(), properties);
    }

    @Test
    void masksLoginPasswordField() {
        String raw = "{\"username\":\"admin\",\"password\":\"StudyOnly@2026\"}";
        String masked = masker.mask(raw, 4096);
        assertTrue(masked.contains("******"));
        assertFalse(masked.contains("StudyOnly@2026"));
    }

    @Test
    void masksOldAndNewPasswordFields() {
        String raw = "{\"oldPassword\":\"Old@123456\",\"newPassword\":\"New@123456\"}";
        String masked = masker.mask(raw, 4096);
        assertTrue(masked.contains("******"));
        assertFalse(masked.contains("Old@123456"));
        assertFalse(masked.contains("New@123456"));
    }

    @Test
    void masksPasswordInsideAccessLogBodyEnvelope() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        var root = mapper.createObjectNode();
        root.set("body", mapper.readTree(
                "{\"tenantCode\":\"demo\",\"username\":\"admin\",\"password\":\"enc:sm2:v1:abcdef\"}"));
        String masked = masker.maskObject(root, 4096);
        assertTrue(masked.contains("******"));
        assertFalse(masked.contains("enc:sm2:v1:abcdef"));
        assertTrue(masked.contains("\"username\":\"admin\""));
    }
}
