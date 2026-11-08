package com.icoolkj.platform.infra.security;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.infra.util.ImageContentValidator;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

/**
 * Lightweight magic-byte sniffing for known extensions.
 */
public final class FileContentSniffer {

    private static final int HEAD_LEN = 512;
    private static final Set<String> IMAGE_EXTS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ZIP_EXTS = Set.of("zip", "docx", "xlsx", "pptx", "jar");
    private static final Set<String> OLE_EXTS = Set.of("doc", "xls", "ppt");

    private FileContentSniffer() {
    }

    public static void validate(Path file, String ext, String contentType) {
        byte[] head = readHead(file);
        validate(head, ext, contentType);
    }

    public static void validate(byte[] head, String ext, String contentType) {
        if (head == null || head.length == 0) {
            return;
        }
        String normalizedExt = ImageContentValidator.normalizeExt(ext);
        if (!StringUtils.hasText(normalizedExt) && StringUtils.hasText(contentType)) {
            normalizedExt = guessExtFromContentType(contentType);
        }
        if (!StringUtils.hasText(normalizedExt)) {
            return;
        }

        if (IMAGE_EXTS.contains(normalizedExt)) {
            if (head.length < 12) {
                throw new BusinessException("error.file.content_mismatch");
            }
            try {
                ImageContentValidator.validateImageBytes(head, normalizedExt);
            } catch (BusinessException ex) {
                throw new BusinessException("error.file.content_mismatch");
            }
            return;
        }

        if ("pdf".equals(normalizedExt)) {
            if (!startsWith(head, "%PDF")) {
                throw new BusinessException("error.file.content_mismatch");
            }
            return;
        }

        if (ZIP_EXTS.contains(normalizedExt)) {
            if (!isZip(head)) {
                throw new BusinessException("error.file.content_mismatch");
            }
            return;
        }

        if (OLE_EXTS.contains(normalizedExt)) {
            if (!isOle(head)) {
                throw new BusinessException("error.file.content_mismatch");
            }
        }
        // Unknown extension: allow (only validate when we know expected magic)
    }

    private static byte[] readHead(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buf = new byte[HEAD_LEN];
            int n = in.read(buf);
            if (n <= 0) {
                return new byte[0];
            }
            if (n == HEAD_LEN) {
                return buf;
            }
            byte[] head = new byte[n];
            System.arraycopy(buf, 0, head, 0, n);
            return head;
        } catch (IOException ex) {
            throw new BusinessException("error.file.content_mismatch");
        }
    }

    private static boolean startsWith(byte[] head, String ascii) {
        byte[] expected = ascii.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        if (head.length < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (head[i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isZip(byte[] head) {
        return head.length >= 4
                && head[0] == 'P'
                && head[1] == 'K'
                && head[2] == 0x03
                && head[3] == 0x04;
    }

    private static boolean isOle(byte[] head) {
        return head.length >= 4
                && (head[0] & 0xFF) == 0xD0
                && (head[1] & 0xFF) == 0xCF
                && (head[2] & 0xFF) == 0x11
                && (head[3] & 0xFF) == 0xE0;
    }

    private static String guessExtFromContentType(String contentType) {
        String ct = contentType.trim().toLowerCase(Locale.ROOT);
        if (ct.contains("jpeg")) {
            return "jpg";
        }
        if (ct.contains("png")) {
            return "png";
        }
        if (ct.contains("gif")) {
            return "gif";
        }
        if (ct.contains("webp")) {
            return "webp";
        }
        if (ct.contains("pdf")) {
            return "pdf";
        }
        if (ct.contains("zip")) {
            return "zip";
        }
        return "";
    }
}
