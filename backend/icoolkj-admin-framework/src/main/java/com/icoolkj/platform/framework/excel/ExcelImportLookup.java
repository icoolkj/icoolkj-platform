package com.icoolkj.platform.framework.excel;

import java.util.List;

/**
 * Supplies dropdown options and resolves human-facing import cells to system codes/ids.
 * System module provides the real bean; framework ships a no-op fallback.
 */
public interface ExcelImportLookup {

    /** Labels for {@link ExcelImportDict} template dropdown (current locale). */
    List<String> dictLabels(String dictType);

    /**
     * Resolves label or code to dict value. Blank → {@code null}.
     * Unknown → {@link com.icoolkj.platform.common.exception.BusinessException}.
     */
    String resolveDictCode(String dictType, String rawLabelOrCode);

    /** Assignable department names for template dropdown (DataScope-aware). */
    List<String> deptNames();

    /**
     * Resolves department name (exact) or in-scope id. Blank → {@code null}.
     * Missing / ambiguous / out of scope → BusinessException.
     */
    String resolveDeptId(String nameOrId);

    /** Active post names for template dropdown. */
    List<String> postNames();

    /**
     * Resolves post name or existing id. Blank → {@code null}.
     * Missing / ambiguous → BusinessException.
     */
    String resolvePostId(String nameOrId);

    /** Active role names for template dropdown. */
    List<String> roleNames();

    /**
     * Resolves role name or existing id. Blank → {@code null}.
     * Missing / ambiguous → BusinessException.
     */
    String resolveRoleId(String nameOrId);
}
