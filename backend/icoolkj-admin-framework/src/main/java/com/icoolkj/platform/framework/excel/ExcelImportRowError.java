package com.icoolkj.platform.framework.excel;

/**
 * One failed Excel import row. {@code rowNumber} is 1-based sheet row (header = 1).
 */
public record ExcelImportRowError(int rowNumber, String message) {
}
