package com.icoolkj.platform.framework.excel;

import java.util.List;

/**
 * Import outcome. Platform default is <strong>all-or-nothing</strong>: when {@link #errorTotal()}
 * &gt; 0, {@link #success()} is 0 and nothing was persisted.
 *
 * <p>{@link #errors()} may be capped at {@link #MAX_REPORTED_ERRORS}; use {@link #errorTotal()} for
 * the true failure count in UI.
 */
public record ExcelImportResult(
        int total,
        int success,
        int skipped,
        int errorTotal,
        List<ExcelImportRowError> errors) {

    public static final int MAX_REPORTED_ERRORS = 50;

    public ExcelImportResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
        if (errorTotal < errors.size()) {
            errorTotal = errors.size();
        }
    }

    public boolean hasErrors() {
        return errorTotal > 0 || !errors.isEmpty();
    }

    public static ExcelImportResult ok(int success, int skipped) {
        return new ExcelImportResult(success + skipped, success, skipped, 0, List.of());
    }

    /** Validation / persist failed — no rows kept (caller must roll back TX if any writes occurred). */
    public static ExcelImportResult rejected(int total, int skipped, List<ExcelImportRowError> errors) {
        int errorTotal = errors == null ? 0 : errors.size();
        List<ExcelImportRowError> capped = errors == null || errors.size() <= MAX_REPORTED_ERRORS
                ? (errors == null ? List.of() : List.copyOf(errors))
                : List.copyOf(errors.subList(0, MAX_REPORTED_ERRORS));
        return new ExcelImportResult(total, 0, skipped, errorTotal, capped);
    }
}
