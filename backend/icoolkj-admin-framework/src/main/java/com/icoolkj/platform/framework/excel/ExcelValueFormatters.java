package com.icoolkj.platform.framework.excel;

import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.framework.i18n.I18nMessageResolver;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Shared formatters for VO → ExportRow mapping (status / datetime / page i18n labels).
 */
public final class ExcelValueFormatters {

    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ExcelValueFormatters() {
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME);
    }

    /** Maps common {@link StatusEnum} codes to localized labels. */
    public static String commonStatus(String status, String normalLabel, String disabledLabel) {
        if (StatusEnum.isNormal(status) && StringUtils.hasText(normalLabel)) {
            return normalLabel;
        }
        if (StatusEnum.isDisabled(status) && StringUtils.hasText(disabledLabel)) {
            return disabledLabel;
        }
        return status;
    }

    /** Maps a binary status code to success/fail labels. */
    public static String binaryStatus(String status,
                                      String successCode,
                                      String failCode,
                                      String successLabel,
                                      String failLabel) {
        if (Objects.equals(successCode, status) && StringUtils.hasText(successLabel)) {
            return successLabel;
        }
        if (Objects.equals(failCode, status) && StringUtils.hasText(failLabel)) {
            return failLabel;
        }
        return status;
    }

    public static String pageText(I18nMessageResolver i18n, String businessName, String pageKey) {
        return ExcelExportSupport.resolvePageText(i18n, businessName, pageKey);
    }

    public static String audit(I18nMessageResolver i18n, String businessName, String category, String code) {
        return ExcelExportSupport.resolveAudit(i18n, businessName, category, code);
    }
}
