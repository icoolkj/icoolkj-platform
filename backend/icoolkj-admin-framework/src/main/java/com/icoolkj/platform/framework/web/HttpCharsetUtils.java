package com.icoolkj.platform.framework.web;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Resolve charset for cached servlet request/response bodies (UTF-8 first). */
public final class HttpCharsetUtils {

    private HttpCharsetUtils() {
    }

    public static String decodeCachedBytes(byte[] content, String contentType, String characterEncoding) {
        if (content == null || content.length == 0) {
            return null;
        }
        return new String(content, resolveCharset(contentType, characterEncoding));
    }

    public static Charset resolveCharset(String contentType, String characterEncoding) {
        Charset fromContentType = charsetFromContentType(contentType);
        if (fromContentType != null) {
            return fromContentType;
        }
        if (StringUtils.hasText(characterEncoding) && !isLatin1Default(characterEncoding)) {
            try {
                return Charset.forName(characterEncoding);
            } catch (Exception ignored) {
                // fall through
            }
        }
        return StandardCharsets.UTF_8;
    }

    private static Charset charsetFromContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return null;
        }
        try {
            return MediaType.parseMediaType(contentType).getCharset();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isLatin1Default(String encoding) {
        return StandardCharsets.ISO_8859_1.name().equalsIgnoreCase(encoding)
                || "ISO8859_1".equalsIgnoreCase(encoding);
    }
}
