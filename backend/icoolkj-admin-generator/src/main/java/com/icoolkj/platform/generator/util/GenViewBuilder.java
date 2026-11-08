package com.icoolkj.platform.generator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.icoolkj.platform.generator.constant.GenConstants;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenTableColumn;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds / merges JSON view schema for generated CRUD pages (locale-agnostic layout).
 * <p>
 * Paired with {@link GenI18nBuilder} i18n JSON under the same {@code i18n/gen/{businessName}/} directory.
 */
public final class GenViewBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final int DEFAULT_QUERY_INPUT_WIDTH = 200;
    private static final int DEFAULT_QUERY_SELECT_WIDTH = 140;
    private static final int DEFAULT_TABLE_MIN_WIDTH = 120;
    private static final int DEFAULT_FORM_SPAN = 24;
    private static final int DEFAULT_TEXTAREA_ROWS = 2;

    /** Column-tab props that must follow codegen on every sync (not manual layout tweaks). */
    private static final Set<String> CODEGEN_FIELD_SYNC_KEYS = Set.of(
            "component", "dictType", "queryType", "insert", "edit", "rows", "valueFormat",
            "treeSource", "treeLabelField", "treeValueField"
    );

    private GenViewBuilder() {
    }

    public static String buildJson(GenTable table, List<GenTableColumn> columns) {
        ObjectNode root = MAPPER.createObjectNode();

        ObjectNode meta = root.putObject("meta");
        meta.put("businessName", table.getBusinessName());
        meta.put("apiModule", table.getApiModule());
        meta.put("backendModule", table.getBackendModule());
        meta.put("permissionPrefix", StringUtils.defaultIfBlank(table.getPermissionPrefix(),
                table.getApiModule() + ":" + table.getBusinessName()));
        meta.put("className", table.getClassName());

        root.putObject("page").put("layout", "standard-crud");

        ObjectNode query = root.putObject("query");
        query.put("maxVisible", VelocityUtils.MAX_QUERY_COLUMNS);
        query.put("collapsible", true);
        ArrayNode queryFields = query.putArray("fields");
        for (GenTableColumn column : VelocityUtils.getQueryColumns(columns, VelocityUtils.MAX_QUERY_COLUMNS)) {
            queryFields.add(buildQueryField(table, column));
        }

        ObjectNode tableNode = root.putObject("table");
        ArrayNode tableFields = tableNode.putArray("fields");
        for (GenTableColumn column : columns) {
            if (!column.isList() || table.isSuperColumn(column.getJavaField())) {
                continue;
            }
            tableFields.add(buildTableField(column));
        }
        ObjectNode actions = tableNode.putObject("actions");
        actions.put("edit", true);
        actions.put("delete", true);
        actions.put("width", 160);

        ObjectNode form = root.putObject("form");
        form.put("mode", "dialog");
        form.put("dialogWidth", 860);
        form.put("labelWidth", 160);
        form.put("closeOnClickModal", false);
        form.put("closeOnPressEscape", false);
        ArrayNode formFields = form.putArray("fields");
        for (GenTableColumn column : columns) {
            if (table.isSuperColumn(column.getJavaField()) || column.isPk()) {
                continue;
            }
            if (!column.isInsert() && !column.isEdit()) {
                continue;
            }
            formFields.add(buildFormField(table, column));
        }

        if (VelocityUtils.hasExportColumns(table) || VelocityUtils.hasImportColumns(table)) {
            ObjectNode extensions = root.putObject("extensions");
            ArrayNode toolbarActions = extensions.putArray("toolbarActions");
            if (VelocityUtils.hasExportColumns(table)) {
                toolbarActions.add(buildToolbarAction("export", "export", "export", true));
            }
            if (VelocityUtils.hasImportColumns(table)) {
                toolbarActions.add(buildToolbarAction("import", "import", "import", false));
            }
        }

        try {
            return GenJsonWriter.write(MAPPER, normalizeView(root));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to build gen view JSON", ex);
        }
    }

    private static ObjectNode buildToolbarAction(String id, String builtin, String permKey, boolean allowOnReadOnly) {
        ObjectNode action = MAPPER.createObjectNode();
        action.put("id", id);
        action.put("builtin", builtin);
        action.put("permKey", permKey);
        if (allowOnReadOnly) {
            action.put("allowOnReadOnly", true);
        }
        return action;
    }

    /** Public helper: set/replace {@code extensions} on a view.json document. */
    public static String withExtensions(String viewJson, JsonNode extensionsNode) {
        try {
            ObjectNode root = StringUtils.isBlank(viewJson)
                    ? MAPPER.createObjectNode()
                    : (ObjectNode) MAPPER.readTree(viewJson);
            if (extensionsNode != null && !extensionsNode.isNull()) {
                if (root.has("extensions") && root.get("extensions").isObject() && extensionsNode.isObject()) {
                    ObjectNode merged = ((ObjectNode) root.get("extensions")).deepCopy();
                    extensionsNode.fields().forEachRemaining(e -> merged.set(e.getKey(), e.getValue().deepCopy()));
                    root.set("extensions", merged);
                } else {
                    root.set("extensions", extensionsNode.deepCopy());
                }
            }
            return GenJsonWriter.write(MAPPER, normalizeView(root));
        } catch (JsonProcessingException ex) {
            return viewJson;
        }
    }

    /** Merge fresh schema into existing, preserving manually edited field layout. */
    public static String mergeJson(String existingJson, String freshJson) {
        if (StringUtils.isBlank(existingJson)) {
            return freshJson;
        }
        try {
            ObjectNode existing = (ObjectNode) MAPPER.readTree(existingJson);
            ObjectNode fresh = (ObjectNode) MAPPER.readTree(freshJson);
            mergeObject(existing, fresh.path("meta"), "meta", true);
            mergeObject(existing, fresh.path("page"), "page");
            mergeObject(existing, fresh.path("query"), "query", "fields");
            mergeObject(existing, fresh.path("table"), "table", "fields");
            mergeObject(existing, fresh.path("form"), "form", "fields");
            mergeExtensions(existing, fresh.path("extensions"));
            return GenJsonWriter.write(MAPPER, normalizeView(existing));
        } catch (JsonProcessingException ex) {
            return freshJson;
        }
    }

    private static void mergeExtensions(ObjectNode existing, JsonNode freshExtensions) {
        if (freshExtensions == null || !freshExtensions.isObject()) {
            return;
        }
        if (!existing.has("extensions") || !existing.get("extensions").isObject()) {
            existing.set("extensions", freshExtensions.deepCopy());
            return;
        }
        ObjectNode existingExt = (ObjectNode) existing.get("extensions");
        ObjectNode freshExt = (ObjectNode) freshExtensions;
        if (freshExt.has("toolbarActions") && freshExt.get("toolbarActions").isArray()) {
            mergeToolbarActions(existingExt, (ArrayNode) freshExt.get("toolbarActions"));
        }
        Iterator<Map.Entry<String, JsonNode>> fields = freshExt.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            if ("toolbarActions".equals(entry.getKey())) {
                continue;
            }
            if (!existingExt.has(entry.getKey())) {
                existingExt.set(entry.getKey(), entry.getValue().deepCopy());
            }
        }
    }

    private static void mergeToolbarActions(ObjectNode existingExt, ArrayNode freshActions) {
        // Preserve hand-tuned toolbars (e.g. export-only). Only seed when absent/empty.
        if (existingExt.has("toolbarActions")
                && existingExt.get("toolbarActions").isArray()
                && !existingExt.get("toolbarActions").isEmpty()) {
            return;
        }
        existingExt.set("toolbarActions", freshActions.deepCopy());
    }

    private static ObjectNode buildQueryField(GenTable table, GenTableColumn column) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("field", column.getJavaField());
        node.put("component", resolveQueryComponent(column));
        node.put("queryType", StringUtils.defaultIfBlank(column.getQueryType(), GenConstants.QUERY_EQ));
        if (StringUtils.isNotBlank(column.getDictType())) {
            node.put("dictType", column.getDictType().trim());
        }
        node.put("width", isTreeLike(column.getHtmlType()) || isSelectLike(column.getHtmlType())
                ? DEFAULT_QUERY_SELECT_WIDTH
                : DEFAULT_QUERY_INPUT_WIDTH);
        applyTreeSelectMeta(node, table, column);
        return node;
    }

    private static ObjectNode buildTableField(GenTableColumn column) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("field", column.getJavaField());
        if (StringUtils.isNotBlank(column.getDictType())) {
            node.put("component", "dict-tag");
            node.put("dictType", column.getDictType().trim());
            node.put("align", "center");
            node.put("minWidth", DEFAULT_TABLE_MIN_WIDTH);
        } else {
            node.put("component", "text");
            node.put("minWidth", DEFAULT_TABLE_MIN_WIDTH);
            node.put("showOverflowTooltip", true);
        }
        return node;
    }

    private static ObjectNode buildFormField(GenTable table, GenTableColumn column) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("field", column.getJavaField());
        node.put("component", resolveFormComponent(column));
        node.put("insert", column.isInsert());
        node.put("edit", column.isEdit());
        node.put("span", DEFAULT_FORM_SPAN);
        if (StringUtils.isNotBlank(column.getDictType())) {
            node.put("dictType", column.getDictType().trim());
        }
        if (GenConstants.HTML_TEXTAREA.equals(column.getHtmlType())) {
            node.put("rows", DEFAULT_TEXTAREA_ROWS);
        }
        if (GenConstants.HTML_DATETIME.equals(column.getHtmlType())) {
            node.put("valueFormat", "YYYY-MM-DD HH:mm:ss");
        }
        applyTreeSelectMeta(node, table, column);
        return node;
    }

    private static void applyTreeSelectMeta(ObjectNode node, GenTable table, GenTableColumn column) {
        if (!"tree-select".equals(node.path("component").asText())) {
            return;
        }
        if ("deptId".equals(column.getJavaField())) {
            node.put("treeSource", "dept");
            node.put("treeLabelField", "deptName");
            node.put("treeValueField", "id");
            return;
        }
        if ("parentId".equals(column.getJavaField())) {
            node.put("treeSource", "self");
            String labelField = "dept".equals(table.getBusinessName()) ? "deptName"
                    : "menu".equals(table.getBusinessName()) ? "menuName" : "label";
            node.put("treeLabelField", labelField);
            node.put("treeValueField", "id");
        }
    }

    private static String resolveQueryComponent(GenTableColumn column) {
        if (GenConstants.HTML_DATETIME.equals(column.getHtmlType())) {
            return "date";
        }
        if (isTreeLike(column.getHtmlType())) {
            return "tree-select";
        }
        if (isSelectLike(column.getHtmlType())) {
            return "select";
        }
        return "input";
    }

    private static String resolveFormComponent(GenTableColumn column) {
        String htmlType = StringUtils.defaultIfBlank(column.getHtmlType(), GenConstants.HTML_INPUT);
        return switch (htmlType) {
            case GenConstants.HTML_TEXTAREA -> "textarea";
            case GenConstants.HTML_SELECT -> "select";
            case GenConstants.HTML_RADIO -> "radio";
            case GenConstants.HTML_DATETIME -> "datetime";
            case GenConstants.HTML_TREE -> "tree-select";
            default -> "input";
        };
    }

    private static boolean isTreeLike(String htmlType) {
        return GenConstants.HTML_TREE.equals(htmlType);
    }

    private static boolean isSelectLike(String htmlType) {
        return GenConstants.HTML_SELECT.equals(htmlType) || GenConstants.HTML_RADIO.equals(htmlType);
    }

    private static void mergeObject(ObjectNode existing, JsonNode fresh, String key, String... arrayKeys) {
        mergeObject(existing, fresh, key, false, arrayKeys);
    }

    private static void mergeObject(ObjectNode existing, JsonNode fresh, String key, boolean overwrite,
                                    String... arrayKeys) {
        if (!fresh.isObject()) {
            return;
        }
        ObjectNode existingSection = existing.has(key) && existing.get(key).isObject()
                ? (ObjectNode) existing.get(key)
                : existing.putObject(key);
        ObjectNode freshSection = (ObjectNode) fresh;
        List<String> arrayKeyList = arrayKeys != null ? List.of(arrayKeys) : List.of();
        Iterator<Map.Entry<String, JsonNode>> fields = freshSection.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode freshValue = entry.getValue();
            if (arrayKeyList.contains(fieldName) && freshValue.isArray()) {
                mergeFieldArray(existingSection, fieldName, (ArrayNode) freshValue);
            } else if (overwrite || !existingSection.has(fieldName)) {
                existingSection.set(fieldName, freshValue.deepCopy());
            }
        }
    }

    private static void mergeFieldArray(ObjectNode section, String arrayKey, ArrayNode freshArray) {
        ArrayNode existingArray = section.has(arrayKey) && section.get(arrayKey).isArray()
                ? (ArrayNode) section.get(arrayKey)
                : section.putArray(arrayKey);
        Map<String, ObjectNode> existingByField = indexByField(existingArray);
        Map<String, ObjectNode> freshByField = indexByField(freshArray);
        ArrayNode merged = MAPPER.createArrayNode();
        List<String> order = new ArrayList<>();
        existingArray.forEach(node -> {
            String field = node.path("field").asText("");
            if (StringUtils.isNotBlank(field) && !order.contains(field)) {
                order.add(field);
            }
        });
        freshByField.keySet().stream().filter(f -> !order.contains(f)).forEach(order::add);
        for (String field : order) {
            ObjectNode existingField = existingByField.get(field);
            ObjectNode freshField = freshByField.get(field);
            if (existingField != null && freshField != null) {
                merged.add(mergeFieldNode(existingField, freshField));
            } else if (existingField != null) {
                merged.add(existingField.deepCopy());
            } else if (freshField != null) {
                merged.add(freshField.deepCopy());
            }
        }
        section.set(arrayKey, merged);
    }

    private static ObjectNode mergeFieldNode(ObjectNode existing, ObjectNode fresh) {
        ObjectNode merged = existing.deepCopy();
        Iterator<Map.Entry<String, JsonNode>> fields = fresh.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            if ("field".equals(key)) {
                continue;
            }
            if (CODEGEN_FIELD_SYNC_KEYS.contains(key)) {
                if (entry.getValue().isNull()) {
                    merged.remove(key);
                } else {
                    merged.set(key, entry.getValue().deepCopy());
                }
                continue;
            }
            if (!merged.has(key)) {
                merged.set(key, entry.getValue().deepCopy());
            }
        }
        for (String key : CODEGEN_FIELD_SYNC_KEYS) {
            if (!fresh.has(key)) {
                merged.remove(key);
            }
        }
        return merged;
    }

    private static Map<String, ObjectNode> indexByField(ArrayNode array) {
        Map<String, ObjectNode> map = new LinkedHashMap<>();
        array.forEach(node -> {
            if (node.isObject()) {
                String field = node.path("field").asText("");
                if (StringUtils.isNotBlank(field)) {
                    map.put(field, (ObjectNode) node);
                }
            }
        });
        return map;
    }

    private static ObjectNode normalizeView(ObjectNode source) {
        return orderObject(source, List.of(
                "meta", "readOnly", "page", "query", "table", "form", "tree", "masterDetail", "extensions"
        ));
    }

    private static JsonNode normalizeNode(String key, JsonNode value) {
        if (value == null || value.isNull()) {
            return value;
        }
        if (value.isArray()) {
            ArrayNode array = MAPPER.createArrayNode();
            value.forEach(item -> array.add(normalizeNode(key, item)));
            return array;
        }
        if (!value.isObject()) {
            return value.deepCopy();
        }

        ObjectNode object = (ObjectNode) value;
        if (object.has("field")) {
            return orderObject(object, List.of(
                    "field", "component", "queryType", "dictType", "width", "minWidth", "align",
                    "showOverflowTooltip", "insert", "edit", "span", "rows", "valueFormat",
                    "disabledOnEdit", "insertOnly", "treeSource", "treeLabelField", "treeValueField"
            ));
        }
        if ("meta".equals(key)) {
            return orderObject(object, List.of(
                    "businessName", "apiModule", "backendModule", "permissionPrefix", "className"
            ));
        }
        if ("page".equals(key)) {
            return orderObject(object, List.of("layout"));
        }
        if ("query".equals(key)) {
            return orderObject(object, List.of("maxVisible", "collapsible", "fields"));
        }
        if ("table".equals(key)) {
            return orderObject(object, List.of("fields", "actions"));
        }
        if ("form".equals(key)) {
            return orderObject(object, List.of(
                    "mode", "dialogWidth", "labelWidth", "closeOnClickModal", "closeOnPressEscape", "fields"
            ));
        }
        if ("extensions".equals(key)) {
            return orderObject(object, List.of("toolbarActions", "rowActions", "dialogs"));
        }
        if ("rowActions".equals(key)) {
            return orderObject(object, List.of(
                    "id", "builtin", "labelKey", "permKey", "type", "handler", "dialog",
                    "dialogRowKey", "dialogIdProp", "allowOnReadOnly", "showWhen", "disabledWhen"
            ));
        }
        if ("toolbarActions".equals(key) || object.has("builtin") && object.has("id") && !object.has("field")) {
            return orderObject(object, List.of(
                    "id", "builtin", "labelKey", "permKey", "type", "fileName",
                    "exportPath", "importPath", "importTemplatePath", "allowOnReadOnly"
            ));
        }
        if ("actions".equals(key)) {
            return orderObject(object, List.of("edit", "delete", "width"));
        }
        return orderObject(object, List.of());
    }

    private static ObjectNode orderObject(ObjectNode source, List<String> preferredKeys) {
        ObjectNode ordered = MAPPER.createObjectNode();
        for (String key : preferredKeys) {
            if (source.has(key)) {
                ordered.set(key, normalizeNode(key, source.get(key)));
            }
        }
        Iterator<Map.Entry<String, JsonNode>> fields = source.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            if (!ordered.has(entry.getKey())) {
                ordered.set(entry.getKey(), normalizeNode(entry.getKey(), entry.getValue()));
            }
        }
        return ordered;
    }
}
