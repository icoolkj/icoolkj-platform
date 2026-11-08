package com.icoolkj.platform.infra.util;

import com.icoolkj.platform.common.exception.BusinessException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/** 图片魔数 / 可读性校验（头像内容安全基础层） */
public final class ImageContentValidator {

    private ImageContentValidator() {
    }

    public static void validateImageBytes(byte[] content, String ext) {
        if (content == null || content.length < 12) {
            throw new BusinessException("error.avatar.invalid_image");
        }
        if (!matchesKnownImageMagic(content)) {
            throw new BusinessException("error.avatar.invalid_image");
        }
        String normalizedExt = normalizeExt(ext);
        if ("gif".equals(normalizedExt) && !isGif(content)) {
            throw new BusinessException("error.avatar.invalid_image");
        }
        if (("jpg".equals(normalizedExt) || "jpeg".equals(normalizedExt)) && !isJpeg(content)) {
            throw new BusinessException("error.avatar.invalid_image");
        }
        if ("png".equals(normalizedExt) && !isPng(content)) {
            throw new BusinessException("error.avatar.invalid_image");
        }
        if ("webp".equals(normalizedExt) && !isWebp(content)) {
            throw new BusinessException("error.avatar.invalid_image");
        }
        try (InputStream in = new ByteArrayInputStream(content)) {
            if (in.read() == -1) {
                throw new BusinessException("error.avatar.invalid_image");
            }
        } catch (IOException ex) {
            throw new BusinessException("error.avatar.invalid_image");
        }
    }

    public static String normalizeExt(String ext) {
        if (ext == null) {
            return "";
        }
        String value = ext.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith(".")) {
            value = value.substring(1);
        }
        return value;
    }

    private static boolean matchesKnownImageMagic(byte[] content) {
        return isJpeg(content) || isPng(content) || isGif(content) || isWebp(content);
    }

    private static boolean isJpeg(byte[] content) {
        return content.length >= 3
                && (content[0] & 0xFF) == 0xFF
                && (content[1] & 0xFF) == 0xD8
                && (content[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(byte[] content) {
        return content.length >= 8
                && content[0] == (byte) 0x89
                && content[1] == 0x50
                && content[2] == 0x4E
                && content[3] == 0x47;
    }

    private static boolean isGif(byte[] content) {
        return content.length >= 6
                && content[0] == 'G'
                && content[1] == 'I'
                && content[2] == 'F';
    }

    private static boolean isWebp(byte[] content) {
        return content.length >= 12
                && content[0] == 'R'
                && content[1] == 'I'
                && content[2] == 'F'
                && content[3] == 'F'
                && content[8] == 'W'
                && content[9] == 'E'
                && content[10] == 'B'
                && content[11] == 'P';
    }
}
