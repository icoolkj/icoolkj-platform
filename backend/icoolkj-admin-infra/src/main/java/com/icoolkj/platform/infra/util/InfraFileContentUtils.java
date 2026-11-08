package com.icoolkj.platform.infra.util;

import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 从 HTML 等内容中提取 infra 文件 ID */
public final class InfraFileContentUtils {

    private static final Pattern FILE_ID_PATTERN = Pattern.compile(
            "(?:/api)?/infra/file/(\\d{10,25})(?:/download)?",
            Pattern.CASE_INSENSITIVE);

    private InfraFileContentUtils() {
    }

    public static Set<String> extractFileIds(String content) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        if (!StringUtils.hasText(content)) {
            return ids;
        }
        Matcher matcher = FILE_ID_PATTERN.matcher(content);
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }
}
