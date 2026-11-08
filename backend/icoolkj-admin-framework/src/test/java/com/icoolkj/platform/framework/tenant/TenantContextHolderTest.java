package com.icoolkj.platform.framework.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantContextHolderTest {

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void exposesTenantContextValues() {
        TenantContextHolder.set(TenantContext.tenant("tenant-1", "demo"));

        assertEquals("tenant-1", TenantContextHolder.getTenantId());
        assertEquals("demo", TenantContextHolder.getTenantCode());
        assertTrue(TenantContextHolder.hasTenant());
        assertFalse(TenantContextHolder.isPlatform());
        assertFalse(TenantContextHolder.isIgnoreTenant());
    }

    @Test
    void exposesPlatformAndIgnoreModes() {
        TenantContextHolder.set(TenantContext.platform());
        assertTrue(TenantContextHolder.isPlatform());

        TenantContextHolder.set(TenantContext.ignore());
        assertTrue(TenantContextHolder.isIgnoreTenant());
        assertFalse(TenantContextHolder.hasTenant());
    }

    @Test
    void clearRemovesContext() {
        TenantContextHolder.set(TenantContext.tenant("tenant-1", "demo"));

        TenantContextHolder.clear();

        assertNull(TenantContextHolder.get());
        assertNull(TenantContextHolder.getTenantId());
    }
}
