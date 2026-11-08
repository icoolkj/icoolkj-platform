package com.icoolkj.platform.framework.excel;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Shared sanitize helpers for Excel download filenames / sheet names. */
public final class ExcelFileNames {

    /** Preferred response header for download filename (UTF-8 display name). */
    public static final String HEADER_DOWNLOAD_FILENAME = "X-Download-Filename";
    /** @deprecated Prefer {@link #HEADER_DOWNLOAD_FILENAME}; kept for older frontends. */
    @Deprecated
    public static final String HEADER_EXPORT_FILENAME = "X-Export-Filename";

    private static final char[] UNSAFE_FILE_CHARS = {'/', '\\', ':', '*', '?', '"', '<', '>', '|'};

    private ExcelFileNames() {
    }

    public static String sanitizeFileBase(String raw, String fallback) {
        if (!StringUtils.hasText(raw)) {
            return fallback;
        }
        String s = raw.trim();
        for (char c : UNSAFE_FILE_CHARS) {
            s = s.replace(c, '_');
        }
        return s.isBlank() ? fallback : s;
    }

    public static String sanitizeAsciiBase(String businessName, String fallback) {
        String s = businessName == null ? fallback : businessName.trim().replaceAll("[^A-Za-z0-9_-]", "_");
        return s.isBlank() ? fallback : s;
    }

    public static String sanitizeSheetName(String raw) {
        String s = raw == null ? "" : raw.replaceAll("[\\\\/?*\\[\\]]", "_").trim();
        if (s.length() > 31) {
            s = s.substring(0, 31);
        }
        return s.isBlank() ? "Sheet1" : s;
    }

    /** Sets Content-Type / Content-Disposition / download filename headers for .xlsx. */
    public static void applyAttachmentHeaders(HttpServletResponse response, String displayName, String asciiName)
            throws Exception {
        String encodedDisplay = URLEncoder.encode(displayName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encodedDisplay);
        response.setHeader(HEADER_DOWNLOAD_FILENAME, displayName);
        response.setHeader(HEADER_EXPORT_FILENAME, displayName);
    }
}
