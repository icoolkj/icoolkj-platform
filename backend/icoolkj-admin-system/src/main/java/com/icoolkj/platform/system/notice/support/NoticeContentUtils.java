package com.icoolkj.platform.system.notice.support;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 通知公告富文本内容规范化与安全过滤 */
public final class NoticeContentUtils {

    private static final String[] FORBIDDEN_TAGS = {
            "script", "iframe", "object", "embed", "link", "meta", "style", "form", "input", "button"
    };

    private NoticeContentUtils() {
    }

    /** 移除 token、blob、危险标签/事件，并统一 infra 路径前缀 */
    public static String sanitize(String content) {
        if (!StringUtils.hasText(content)) {
            return content;
        }
        String cleaned = normalizeInfraPaths(stripAccessTokens(content));
        Document doc = Jsoup.parseBodyFragment(cleaned);
        for (String tag : FORBIDDEN_TAGS) {
            doc.select(tag).remove();
        }
        for (Element element : doc.getAllElements()) {
            sanitizeElementAttributes(element);
        }
        return doc.body().html();
    }

    private static String stripAccessTokens(String content) {
        String cleaned = content
                .replaceAll("([?&])access_token=[^&\"'\\s<>]+", "$1")
                .replaceAll("\\?&", "?")
                .replaceAll("[?&](?=\\s|>|\"|')", "")
                .replaceAll("\\?&", "?");
        if (cleaned.endsWith("?") || cleaned.endsWith("&")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        cleaned = cleaned.replaceAll("\\sdata-href=\"blob:[^\"]*\"", "");
        cleaned = cleaned.replaceAll("\\sdata-href='blob:[^']*'", "");
        return cleaned;
    }

    private static String normalizeInfraPaths(String content) {
        return content.replace("/api/infra/file/", "/infra/file/")
                .replace("/api/system/notice/content-file/", "/system/notice/content-file/");
    }

    private static void sanitizeElementAttributes(Element element) {
        List<Attribute> attrs = new ArrayList<>(element.attributes().asList());
        for (Attribute attr : attrs) {
            String key = attr.getKey().toLowerCase(Locale.ROOT);
            String value = attr.getValue();
            if (key.startsWith("on") || "data-infra-canonical-src".equals(key)) {
                element.removeAttr(attr.getKey());
                continue;
            }
            if (!("src".equals(key) || "href".equals(key))) {
                continue;
            }
            if (!StringUtils.hasText(value)) {
                continue;
            }
            String trimmed = value.trim();
            if (trimmed.startsWith("blob:") || trimmed.startsWith("data:") || trimmed.startsWith("javascript:")) {
                element.removeAttr(attr.getKey());
                continue;
            }
            if ("src".equals(key) || "href".equals(key)) {
                element.attr(attr.getKey(), normalizeAttrUrl(trimmed));
            }
        }
    }

    private static String normalizeAttrUrl(String url) {
        String normalized = url.replace("/api/infra/file/", "/infra/file/");
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("/system/notice/content-file/(\\d{10,25})/download")
                .matcher(normalized);
        if (matcher.find()) {
            String inline = normalized.contains("inline=1") ? "?inline=1" : "";
            return "/infra/file/" + matcher.group(1) + "/download" + inline;
        }
        return normalized;
    }
}
