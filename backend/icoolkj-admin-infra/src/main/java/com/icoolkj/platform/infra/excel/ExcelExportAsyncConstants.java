package com.icoolkj.platform.infra.excel;

/** Shared constants for async Excel export staging + file refs. */
public final class ExcelExportAsyncConstants {

    /** Reserved: only async export handlers may bind this bizType. */
    public static final String BIZ_TYPE = "excel-export";
    public static final String REF_ROLE = "export";

    private ExcelExportAsyncConstants() {
    }
}
