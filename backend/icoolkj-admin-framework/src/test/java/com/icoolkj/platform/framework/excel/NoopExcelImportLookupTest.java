package com.icoolkj.platform.framework.excel;

import com.icoolkj.platform.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoopExcelImportLookupTest {

    private final NoopExcelImportLookup lookup = new NoopExcelImportLookup();

    @Test
    void blankResolveReturnsNull() {
        assertNull(lookup.resolveDictCode("sys_gender", null));
        assertNull(lookup.resolveDictCode("sys_gender", "  "));
        assertNull(lookup.resolveDeptId(null));
        assertNull(lookup.resolveDeptId(""));
    }

    @Test
    void nonBlankResolveFailsClosed() {
        BusinessException dictEx = assertThrows(
                BusinessException.class, () -> lookup.resolveDictCode("sys_gender", "男"));
        assertEquals("error.excel.import_dict_invalid", dictEx.getMessage());

        BusinessException deptEx = assertThrows(
                BusinessException.class, () -> lookup.resolveDeptId("研发部"));
        assertEquals("error.excel.import_dept_not_found", deptEx.getMessage());

        BusinessException postEx = assertThrows(
                BusinessException.class, () -> lookup.resolvePostId("工程师"));
        assertEquals("error.excel.import_post_not_found", postEx.getMessage());

        BusinessException roleEx = assertThrows(
                BusinessException.class, () -> lookup.resolveRoleId("管理员"));
        assertEquals("error.excel.import_role_not_found", roleEx.getMessage());
    }

    @Test
    void dropdownsEmpty() {
        assertEquals(0, lookup.dictLabels("sys_gender").size());
        assertEquals(0, lookup.deptNames().size());
        assertEquals(0, lookup.postNames().size());
        assertEquals(0, lookup.roleNames().size());
    }
}
