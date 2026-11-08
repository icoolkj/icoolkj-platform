package com.icoolkj.platform.infra.fileprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class FileUploadProfileResolverTest {

    @Test
    void intersectOrTighten_tenantMustSubsetPlatform() {
        List<String> platform = List.of("png", "jpg", "pdf");
        List<String> tenant = List.of("png", "gif");
        List<String> merged = FileUploadProfileResolver.intersectOrTighten(platform, tenant);
        assertEquals(List.of("png"), merged);
    }

    @Test
    void clampMaxSize_cannotExceedPlatform() {
        assertEquals(1000L, FileUploadProfileResolver.clampMaxSize(1000L, 5000L));
        assertEquals(500L, FileUploadProfileResolver.clampMaxSize(1000L, 500L));
        assertEquals(500L, FileUploadProfileResolver.clampMaxSize(0L, 500L));
    }

    @Test
    void parseCsvLower_normalizes() {
        List<String> parts = FileUploadProfileResolver.parseCsvLower("PNG, Jpg ,WEBP");
        assertTrue(parts.contains("png"));
        assertTrue(parts.contains("jpg"));
        assertTrue(parts.contains("webp"));
    }
}
