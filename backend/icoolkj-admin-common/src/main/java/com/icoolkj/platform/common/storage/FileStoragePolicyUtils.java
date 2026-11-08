package com.icoolkj.platform.common.storage;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class FileStoragePolicyUtils {

    private FileStoragePolicyUtils() {
    }

    public static List<String> splitCsv(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(v -> v.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
    }

    public static String normalizeExtension(String ext) {
        if (!StringUtils.hasText(ext)) {
            return "";
        }
        String normalized = ext.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith(".") ? normalized.substring(1) : normalized;
    }
}
