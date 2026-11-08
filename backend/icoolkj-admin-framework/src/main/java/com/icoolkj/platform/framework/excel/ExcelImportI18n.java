package com.icoolkj.platform.framework.excel;

import com.icoolkj.platform.common.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import org.springframework.util.StringUtils;

/**
 * Encode Excel row / async error payloads as {@code key} + optional US-unit-separated args
 * so the UI can translate with the viewer's locale (do not bake worker locale into result JSON).
 */
public final class ExcelImportI18n {

    public static final char ARG_SEP = '\u001f';

    private ExcelImportI18n() {
    }

    public static String encode(String key, Object... args) {
        String k = StringUtils.hasText(key) ? key.trim() : "error.excel.import_failed";
        if (args == null || args.length == 0) {
            return k;
        }
        StringBuilder sb = new StringBuilder(k);
        for (Object arg : args) {
            sb.append(ARG_SEP).append(arg == null ? "" : String.valueOf(arg));
        }
        return sb.toString();
    }

    public static String encode(BusinessException ex) {
        if (ex == null) {
            return "error.excel.import_failed";
        }
        return encode(ex.getMessage(), ex.getArgs() == null ? new Object[0] : ex.getArgs());
    }

    /**
     * Prefer Bean Validation {@code messageTemplate} (e.g. {@code {gen.user.username.required}})
     * so async import does not bake the worker locale into {@code ExcelImportResult}.
     */
    public static String encodeViolation(ConstraintViolation<?> violation) {
        if (violation == null) {
            return "error.excel.import_failed";
        }
        String template = violation.getMessageTemplate();
        String key = unwrapBraceKey(template);
        if (looksLikeKey(key)) {
            return encode(key);
        }
        String message = violation.getMessage();
        if (looksLikeKey(message)) {
            return encode(message.trim());
        }
        String path = violation.getPropertyPath() == null ? "" : violation.getPropertyPath().toString();
        return encode("error.excel.import_row_invalid", path);
    }

    static String unwrapBraceKey(String template) {
        if (!StringUtils.hasText(template)) {
            return "";
        }
        String t = template.trim();
        if (t.startsWith("{") && t.endsWith("}") && t.length() > 2) {
            return t.substring(1, t.length() - 1).trim();
        }
        return t;
    }

    /** True when {@code raw} looks like an i18n key (with optional encoded args). */
    public static boolean looksLikeKey(String raw) {
        if (!StringUtils.hasText(raw)) {
            return false;
        }
        String key = raw.trim();
        int sep = key.indexOf(ARG_SEP);
        if (sep >= 0) {
            key = key.substring(0, sep);
        }
        return key.startsWith("error.")
                || key.startsWith("common.")
                || key.startsWith("validation.")
                || key.startsWith("gen.");
    }
}
