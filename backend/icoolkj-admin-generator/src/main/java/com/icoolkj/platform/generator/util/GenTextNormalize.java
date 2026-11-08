package com.icoolkj.platform.generator.util;

/**
 * Normalizes generated text so frontend artifacts match Prettier
 * ({@code endOfLine: "lf"}, {@code insert_final_newline}).
 */
public final class GenTextNormalize {

    private GenTextNormalize() {
    }

    public static String toLfWithTrailingNewline(String content) {
        if (content == null || content.isEmpty()) {
            return content == null ? null : "\n";
        }
        String normalized = content.replace("\r\n", "\n").replace("\r", "\n");
        return normalized.endsWith("\n") ? normalized : normalized + "\n";
    }
}
