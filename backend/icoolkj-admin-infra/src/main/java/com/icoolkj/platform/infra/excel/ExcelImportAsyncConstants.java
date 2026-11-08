package com.icoolkj.platform.infra.excel;

/** Shared constants for async Excel import staging + file refs. */
public final class ExcelImportAsyncConstants {

    /** Reserved: only {@link ExcelImportAsyncSubmitFacade} may bind this bizType. */
    public static final String BIZ_TYPE = "excel-import";
    public static final String REF_ROLE = "import";

    private ExcelImportAsyncConstants() {
    }

    /** True for platform-reserved Excel staging bizTypes (import + export). */
    public static boolean isReservedBizType(String bizType) {
        if (bizType == null || bizType.isBlank()) {
            return false;
        }
        String t = bizType.trim();
        return BIZ_TYPE.equals(t) || ExcelExportAsyncConstants.BIZ_TYPE.equals(t);
    }
}
