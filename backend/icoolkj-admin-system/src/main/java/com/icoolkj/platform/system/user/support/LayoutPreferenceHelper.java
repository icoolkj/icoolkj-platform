package com.icoolkj.platform.system.user.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.system.user.vo.LayoutPreferenceVO;
import org.springframework.util.StringUtils;

/** Parse / merge / serialize {@code ui.layout} preference JSON. */
public final class LayoutPreferenceHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LayoutPreferenceHelper() {
    }

    public static LayoutPreferenceVO defaults() {
        return sanitize(new LayoutPreferenceVO());
    }

    public static LayoutPreferenceVO parse(String json) {
        if (!StringUtils.hasText(json)) {
            return defaults();
        }
        try {
            LayoutPreferenceVO parsed = MAPPER.readValue(json.trim(), LayoutPreferenceVO.class);
            return sanitize(parsed);
        } catch (JsonProcessingException ex) {
            return defaults();
        }
    }

    public static String toJson(LayoutPreferenceVO layout) {
        try {
            return MAPPER.writeValueAsString(sanitize(layout));
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    public static void merge(LayoutPreferenceVO target, LayoutPreferenceVO patch) {
        if (patch == null) {
            return;
        }
        if (StringUtils.hasText(patch.getSidebarTheme())) {
            target.setSidebarTheme(patch.getSidebarTheme());
        }
        if (patch.getBreadcrumb() != null) {
            target.setBreadcrumb(patch.getBreadcrumb());
        }
        if (patch.getTagsView() != null) {
            target.setTagsView(patch.getTagsView());
        }
        if (StringUtils.hasText(patch.getFormMode())) {
            target.setFormMode(patch.getFormMode());
        }
        if (patch.getWatermark() != null) {
            target.setWatermark(patch.getWatermark());
        }
        if (patch.getMenuUniqueOpened() != null) {
            target.setMenuUniqueOpened(patch.getMenuUniqueOpened());
        }
        if (patch.getPageHeader() != null) {
            target.setPageHeader(patch.getPageHeader());
        }
        if (StringUtils.hasText(patch.getHeaderStyle())) {
            target.setHeaderStyle(patch.getHeaderStyle());
        }
        sanitize(target);
    }

    private static LayoutPreferenceVO sanitize(LayoutPreferenceVO layout) {
        LayoutPreferenceVO safe = layout != null ? layout : defaults();
        if (!"light".equals(safe.getSidebarTheme())) {
            safe.setSidebarTheme("dark");
        }
        if (!"drawer".equals(safe.getFormMode())) {
            safe.setFormMode("dialog");
        }
        if (safe.getBreadcrumb() == null) {
            safe.setBreadcrumb(true);
        }
        if (safe.getTagsView() == null) {
            safe.setTagsView(true);
        }
        if (safe.getWatermark() == null) {
            safe.setWatermark(true);
        }
        if (safe.getMenuUniqueOpened() == null) {
            safe.setMenuUniqueOpened(true);
        }
        if (safe.getPageHeader() == null) {
            safe.setPageHeader(false);
        }
        if (!"light".equals(safe.getHeaderStyle())) {
            safe.setHeaderStyle("primary");
        }
        return safe;
    }
}
