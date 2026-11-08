package com.icoolkj.platform.system.datascope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DataScopeMatchSupportTest {

    @ParameterizedTest
    @CsvSource({
            "true,  true,  u1, d1, u1, d9, true",
            "true,  true,  u1, d1, u2, d1, false",
            "false, true,  u1, d1, u2, d1, true",
            "false, true,  u1, '', u2, d1, false",
            "false, false, u1, d1, u1, d1, false"
    })
    void matchesSelfMatrix(boolean hasUser,
                            boolean hasDept,
                            String ownerUser,
                            String ownerDept,
                            String opUser,
                            String opDept,
                            boolean expected) {
        assertEquals(expected, DataScopeMatchSupport.matchesSelf(
                hasUser, hasDept, ownerUser, ownerDept, opUser, opDept));
    }

    @Test
    void matchesDeptDeniesEmptyOwnerDept() {
        assertFalse(DataScopeMatchSupport.matchesDept(
                true, true, "u1", null, "u2", "d1"));
        assertFalse(DataScopeMatchSupport.matchesDept(
                true, true, "u1", "", "u2", "d1"));
        assertTrue(DataScopeMatchSupport.matchesDept(
                true, true, "u1", "d1", "u2", "d1"));
    }

    @Test
    void matchesDeptAndChildUsesAncestorsLikeFindInSet() {
        assertTrue(DataScopeMatchSupport.matchesDeptAndChild(
                true, true, "u1", "child", "0,root,d1", "u2", "d1"));
        assertFalse(DataScopeMatchSupport.matchesDeptAndChild(
                true, true, "u1", "child", "0,root,other", "u2", "d1"));
        assertFalse(DataScopeMatchSupport.matchesDeptAndChild(
                true, true, "u1", "", "0,d1", "u2", "d1"));
        assertTrue(DataScopeMatchSupport.matchesDeptAndChild(
                true, true, "u1", "d1", null, "u2", "d1"));
    }

    @Test
    void matchesCustomEmptyOwnerDenyAndEmptyAllowedFallback() {
        assertEquals(Boolean.FALSE, DataScopeMatchSupport.matchesCustomOrFallbackSelf(null, List.of("d1")));
        assertNull(DataScopeMatchSupport.matchesCustomOrFallbackSelf("d1", List.of()));
        assertEquals(Boolean.TRUE, DataScopeMatchSupport.matchesCustomOrFallbackSelf("d1", List.of("d0", "d1")));
        assertEquals(Boolean.FALSE, DataScopeMatchSupport.matchesCustomOrFallbackSelf("d2", List.of("d1")));
    }
}
