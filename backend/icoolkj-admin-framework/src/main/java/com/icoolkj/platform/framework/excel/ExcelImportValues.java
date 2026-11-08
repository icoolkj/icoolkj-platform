package com.icoolkj.platform.framework.excel;

import com.icoolkj.platform.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Pure helpers for dict label/code resolution (unit-testable). */
public final class ExcelImportValues {

    private ExcelImportValues() {
    }

    public record LabelValue(String label, String value) {
    }

    /**
     * Accepts exact value, exact label (case-sensitive), or {@code label(value)} display form.
     */
    public static String resolveDictCode(String dictType, List<LabelValue> items, String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String text = raw.trim();
        if (items == null || items.isEmpty()) {
            throw new BusinessException("error.excel.import_dict_invalid", dictType, text);
        }
        for (LabelValue item : items) {
            if (item == null || !StringUtils.hasText(item.value())) {
                continue;
            }
            if (Objects.equals(item.value(), text) || Objects.equals(item.label(), text)) {
                return item.value();
            }
            if (StringUtils.hasText(item.label()) && text.equals(item.label() + "(" + item.value() + ")")) {
                return item.value();
            }
        }
        throw new BusinessException("error.excel.import_dict_invalid", dictType, text);
    }

    public static String resolveExactName(String name,
                                          List<String> candidates,
                                          String notFoundKey,
                                          String ambiguousKey) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        String text = name.trim();
        if (candidates == null || candidates.isEmpty()) {
            throw new BusinessException(notFoundKey, text);
        }
        List<String> hits = candidates.stream()
                .filter(Objects::nonNull)
                .filter(c -> c.equals(text))
                .toList();
        if (hits.isEmpty()) {
            // case-insensitive fallback for ascii names
            String lower = text.toLowerCase(Locale.ROOT);
            hits = candidates.stream()
                    .filter(Objects::nonNull)
                    .filter(c -> c.toLowerCase(Locale.ROOT).equals(lower))
                    .toList();
        }
        if (hits.isEmpty()) {
            throw new BusinessException(notFoundKey, text);
        }
        if (hits.size() > 1) {
            throw new BusinessException(ambiguousKey, text);
        }
        return hits.get(0);
    }

    public static Map<String, String> indexByName(List<String> names, List<String> ids) {
        Map<String, String> map = new LinkedHashMap<>();
        if (names == null || ids == null) {
            return map;
        }
        int n = Math.min(names.size(), ids.size());
        for (int i = 0; i < n; i++) {
            if (StringUtils.hasText(names.get(i)) && StringUtils.hasText(ids.get(i))) {
                map.putIfAbsent(names.get(i).trim(), ids.get(i).trim());
            }
        }
        return map;
    }

    /**
     * Resolves human name or existing id from (name, id) entries.
     * Exact name first, then case-insensitive; duplicate names → ambiguous.
     */
    public static String resolveNameOrId(String nameOrId,
                                         List<Map.Entry<String, String>> entries,
                                         String notFoundKey,
                                         String ambiguousKey) {
        if (!StringUtils.hasText(nameOrId)) {
            return null;
        }
        String text = nameOrId.trim();
        Map<String, List<String>> nameToIds = new LinkedHashMap<>();
        Map<String, String> idSet = new LinkedHashMap<>();
        if (entries != null) {
            for (Map.Entry<String, String> e : entries) {
                if (e == null || !StringUtils.hasText(e.getKey()) || !StringUtils.hasText(e.getValue())) {
                    continue;
                }
                String name = e.getKey().trim();
                String id = e.getValue().trim();
                nameToIds.computeIfAbsent(name, k -> new java.util.ArrayList<>()).add(id);
                idSet.putIfAbsent(id, name);
            }
        }
        if (idSet.containsKey(text)) {
            return text;
        }
        List<String> ids = nameToIds.get(text);
        if (ids == null || ids.isEmpty()) {
            String lower = text.toLowerCase(Locale.ROOT);
            for (Map.Entry<String, List<String>> e : nameToIds.entrySet()) {
                if (e.getKey().toLowerCase(Locale.ROOT).equals(lower)) {
                    ids = e.getValue();
                    break;
                }
            }
        }
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(notFoundKey, text);
        }
        if (ids.size() > 1) {
            throw new BusinessException(ambiguousKey, text);
        }
        return ids.get(0);
    }
}
