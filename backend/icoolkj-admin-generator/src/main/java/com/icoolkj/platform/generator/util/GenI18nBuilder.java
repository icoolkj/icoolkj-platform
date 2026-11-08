package com.icoolkj.platform.generator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.generator.constant.GenConstants;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenTableColumn;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds / merges gen i18n JSON from gen_table columns.
 * <p>
 * Copy is driven by classpath templates {@code i18n/gen/_codegen/{locale}.json}
 * (placeholders {@code {functionName}}, {@code {label}}, {@code {maxLength}}, …).
 * Java must not hardcode UI strings — edit the JSON templates instead.
 * <p>
 * Schema of generated business JSON:
 * <pre>
 * {
 *   "page": { "title", "addTitle", "editTitle", "deleteConfirm", "exportFileName", "exportSheetName", ... },
 *   "fields": {
 *     "orderNo": {
 *       "label", "placeholder",
 *       "validation": { "required", "maxLength", ... }
 *     }
 *   },
 *   "meta": { "businessName", "apiModule", "functionName", "constraints": { ... } }
 * }
 * </pre>
 */
public final class GenI18nBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern VARCHAR_LENGTH = Pattern.compile("varchar\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHAR_LENGTH = Pattern.compile("char\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);
    private static final String CODEGEN_TEMPLATE_BUSINESS = "_codegen";

    private GenI18nBuilder() {
    }

    public static String buildJson(GenTable table, List<GenTableColumn> columns, String locale) {
        String normalized = LocaleResolver.normalize(locale);
        boolean en = LocaleResolver.LOCALE_EN_US.equals(normalized);
        JsonNode templateRoot = loadCodegenTemplate(normalized);
        JsonNode pageTemplates = templateRoot.path("page");
        JsonNode phraseTemplates = templateRoot.path("templates");

        String functionName = resolveFunctionName(table, en, phraseTemplates);
        Map<String, String> pageVars = new HashMap<>();
        pageVars.put("functionName", functionName);
        pageVars.put("functionNameLower", functionName.toLowerCase(Locale.ROOT));
        pageVars.put("count", "{count}");

        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode page = root.putObject("page");
        Iterator<String> pageKeys = pageTemplates.fieldNames();
        while (pageKeys.hasNext()) {
            String key = pageKeys.next();
            String raw = pageTemplates.path(key).asText("");
            if (StringUtils.isNotBlank(raw)) {
                page.put(key, fill(raw, pageVars));
            }
        }

        ObjectNode fields = root.putObject("fields");
        ObjectNode constraints = root.putObject("meta").putObject("constraints");
        ObjectNode meta = (ObjectNode) root.get("meta");
        meta.put("businessName", table.getBusinessName());
        meta.put("apiModule", table.getApiModule());
        meta.put("functionName", functionName);

        for (GenTableColumn column : columns) {
            if (table.isSuperColumn(column.getJavaField())) {
                continue;
            }
            if (!column.isList() && !column.isInsert() && !column.isEdit() && !column.isQuery()
                    && !column.isExport() && !column.isImport()) {
                continue;
            }
            String label = resolveLabel(column, en);
            ObjectNode field = fields.putObject(column.getJavaField());
            field.put("label", label);
            if (column.isQuery() || column.isInsert() || column.isEdit()) {
                field.put("placeholder", buildPlaceholder(column, label, phraseTemplates));
            }

            ObjectNode constraint = constraints.putObject(column.getJavaField());
            ObjectNode validation = field.putObject("validation");
            applyValidation(table, column, label, phraseTemplates, constraint, validation);
        }

        try {
            return GenJsonWriter.write(MAPPER, normalizeI18n(root));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to build gen i18n JSON", ex);
        }
    }

    private static JsonNode loadCodegenTemplate(String locale) {
        String json = GenClasspathBaselineLoader.loadI18nJson(CODEGEN_TEMPLATE_BUSINESS, locale)
                .or(() -> GenClasspathBaselineLoader.loadI18nJson(
                        CODEGEN_TEMPLATE_BUSINESS, LocaleResolver.LOCALE_ZH_CN))
                .orElseThrow(() -> new IllegalStateException(
                        "Missing codegen i18n template: i18n/gen/_codegen/" + locale + ".json"));
        try {
            JsonNode root = MAPPER.readTree(json);
            if (!root.path("page").isObject() || !root.path("templates").isObject()) {
                throw new IllegalStateException("Invalid _codegen template: page/templates required");
            }
            return root;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Invalid _codegen template JSON", ex);
        }
    }

    private static String fill(String template, Map<String, String> vars) {
        if (StringUtils.isBlank(template)) {
            return "";
        }
        String result = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return result;
    }

    private static String templatePhrase(JsonNode phraseTemplates, String key) {
        String value = phraseTemplates.path(key).asText("");
        if (StringUtils.isBlank(value)) {
            throw new IllegalStateException("Missing _codegen templates." + key);
        }
        return value;
    }

    /**
     * Merge newly built JSON into existing, preserving manually edited labels/validation text.
     */
    public static String mergeJson(String existingJson, String freshJson, String locale) {
        if (StringUtils.isBlank(existingJson)) {
            return freshJson;
        }
        try {
            ObjectNode existing = (ObjectNode) MAPPER.readTree(existingJson);
            ObjectNode fresh = (ObjectNode) MAPPER.readTree(freshJson);
            boolean refreshStaleEnglish = LocaleResolver.LOCALE_EN_US.equals(LocaleResolver.normalize(locale));

            mergeObject(existing, fresh.path("page"), "page", refreshStaleEnglish);
            mergeFields(existing, fresh, refreshStaleEnglish);
            mergeMeta(existing, fresh, refreshStaleEnglish);
            return GenJsonWriter.write(MAPPER, normalizeI18n(existing));
        } catch (JsonProcessingException ex) {
            return freshJson;
        }
    }

    /**
     * Flatten gen i18n JSON for backend MessageSource:
     * <ul>
     *   <li>{@code gen.{business}.{field}.{rule}} — Bean Validation</li>
     *   <li>{@code gen.{business}.{field}.label} — field labels (Excel / forms)</li>
     *   <li>{@code gen.{business}.page.{key}} — page texts (export file/sheet names)</li>
     *   <li>{@code gen.{business}.audit.modules|businessTypes.{code}} — audit labels</li>
     * </ul>
     */
    public static Map<String, String> flattenValidationKeys(String messagesJson) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isBlank(messagesJson)) {
            return map;
        }
        try {
            JsonNode root = MAPPER.readTree(messagesJson);
            String businessName = root.path("meta").path("businessName").asText("");
            if (StringUtils.isBlank(businessName)) {
                return map;
            }
            String prefix = "gen." + businessName + ".";

            JsonNode page = root.path("page");
            if (page.isObject()) {
                Iterator<String> pageKeys = page.fieldNames();
                while (pageKeys.hasNext()) {
                    String key = pageKeys.next();
                    JsonNode node = page.path(key);
                    if (node.isTextual() && StringUtils.isNotBlank(node.asText())) {
                        map.put(prefix + "page." + key, node.asText());
                    }
                }
            }

            JsonNode fields = root.path("fields");
            Iterator<String> names = fields.fieldNames();
            while (names.hasNext()) {
                String field = names.next();
                JsonNode fieldNode = fields.path(field);
                String label = fieldNode.path("label").asText("");
                if (StringUtils.isNotBlank(label)) {
                    map.put(prefix + field + ".label", label);
                }
                JsonNode validation = fieldNode.path("validation");
                Iterator<String> rules = validation.fieldNames();
                while (rules.hasNext()) {
                    String rule = rules.next();
                    String value = validation.path(rule).asText("");
                    if (StringUtils.isNotBlank(value)) {
                        map.put(prefix + field + "." + rule, value);
                    }
                }
            }

            JsonNode audit = root.path("audit");
            if (audit.isObject()) {
                flattenAuditMap(map, prefix + "audit.modules.", audit.path("modules"));
                flattenAuditMap(map, prefix + "audit.businessTypes.", audit.path("businessTypes"));
            }

            JsonNode keys = root.path("keys");
            if (keys.isObject()) {
                Iterator<String> keyNames = keys.fieldNames();
                while (keyNames.hasNext()) {
                    String name = keyNames.next();
                    String value = keys.path(name).asText("");
                    if (StringUtils.isNotBlank(value)) {
                        map.put(prefix + "keys." + name, value);
                    }
                }
            }
        } catch (JsonProcessingException ignored) {
            // skip malformed
        }
        return map;
    }

    private static void flattenAuditMap(Map<String, String> map, String keyPrefix, JsonNode node) {
        if (node == null || !node.isObject()) {
            return;
        }
        Iterator<String> names = node.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            String value = node.path(name).asText("");
            if (StringUtils.isNotBlank(value)) {
                map.put(keyPrefix + name, value);
            }
        }
    }

    /** Read a string from {@code page.{key}} in gen i18n JSON. */
    public static String readPageText(String messagesJson, String key) {
        if (StringUtils.isBlank(messagesJson) || StringUtils.isBlank(key)) {
            return "";
        }
        try {
            JsonNode value = MAPPER.readTree(messagesJson).path("page").path(key);
            return value.isMissingNode() || value.isNull() ? "" : value.asText("");
        } catch (JsonProcessingException ignored) {
            return "";
        }
    }

    public static Integer parseColumnLength(String columnType) {
        if (StringUtils.isBlank(columnType)) {
            return null;
        }
        Matcher varchar = VARCHAR_LENGTH.matcher(columnType);
        if (varchar.find()) {
            return Integer.parseInt(varchar.group(1));
        }
        Matcher charType = CHAR_LENGTH.matcher(columnType);
        if (charType.find()) {
            return Integer.parseInt(charType.group(1));
        }
        return null;
    }

    private static void applyValidation(GenTable table, GenTableColumn column, String label,
                                        JsonNode phraseTemplates,
                                        ObjectNode constraint, ObjectNode validation) {
        boolean required = column.isRequired()
                && (column.isInsert() || column.isEdit())
                && !column.isPk()
                && !table.isSuperColumn(column.getJavaField());
        if (required) {
            constraint.put("required", true);
            Map<String, String> vars = Map.of("label", label);
            validation.put("required", fill(templatePhrase(phraseTemplates, "validationRequired"), vars));
        }
        Integer maxLength = column.getColumnLength();
        if (maxLength == null) {
            maxLength = parseColumnLength(column.getColumnType());
        }
        if ("String".equals(column.getJavaType()) && maxLength != null && maxLength > 0) {
            constraint.put("maxLength", maxLength);
            Map<String, String> vars = new HashMap<>();
            vars.put("label", label);
            vars.put("maxLength", String.valueOf(maxLength));
            validation.put("maxLength", fill(templatePhrase(phraseTemplates, "validationMaxLength"), vars));
        }
    }

    private static String resolveLabel(GenTableColumn column, boolean en) {
        if (!en) {
            return StringUtils.defaultIfBlank(column.getColumnComment(), column.getJavaField());
        }
        if (StringUtils.isNotBlank(column.getJavaField())) {
            return camelCaseToEnglishLabel(column.getJavaField());
        }
        if (StringUtils.isNotBlank(column.getColumnName())) {
            return camelCaseToEnglishLabel(column.getColumnName());
        }
        return toEnglishTitle(null, column.getColumnComment());
    }

    private static String buildPlaceholder(GenTableColumn column, String label, JsonNode phraseTemplates) {
        return buildPlaceholderByComponent(resolvePlaceholderComponent(column), label, phraseTemplates);
    }

    /** Align with {@link GenViewBuilder} query/form component mapping. */
    private static String resolvePlaceholderComponent(GenTableColumn column) {
        if (column.isInsert() || column.isEdit()) {
            return resolveFormComponent(column);
        }
        if (column.isQuery()) {
            return resolveQueryComponent(column);
        }
        return "input";
    }

    private static String resolveQueryComponent(GenTableColumn column) {
        if (GenConstants.HTML_DATETIME.equals(column.getHtmlType())) {
            return "date";
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

    private static boolean isSelectLike(String htmlType) {
        return GenConstants.HTML_SELECT.equals(htmlType) || GenConstants.HTML_RADIO.equals(htmlType);
    }

    private static String buildPlaceholderByComponent(String component, String label, JsonNode phraseTemplates) {
        Map<String, String> vars = Map.of("label", label);
        if ("select".equals(component) || "radio".equals(component)
                || "tree-select".equals(component) || "date".equals(component)
                || "datetime".equals(component)) {
            return fill(templatePhrase(phraseTemplates, "placeholderSelect"), vars);
        }
        return fill(templatePhrase(phraseTemplates, "placeholderInput"), vars);
    }

    private static String resolveFunctionName(GenTable table, boolean en, JsonNode phraseTemplates) {
        String functionName = StringUtils.defaultIfBlank(table.getFunctionName(), table.getBusinessName());
        if (!en) {
            return StringUtils.defaultIfBlank(functionName, templatePhrase(phraseTemplates, "functionNameFallback"));
        }
        String englishName = toEnglishTitle(table.getClassName(), table.getBusinessName());
        if (StringUtils.isNotBlank(englishName)) {
            return englishName;
        }
        englishName = toEnglishTitle(null, functionName);
        if (StringUtils.isNotBlank(englishName)) {
            return englishName;
        }
        return templatePhrase(phraseTemplates, "functionNameFallback");
    }

    private static String toEnglishTitle(String className, String fallback) {
        if (StringUtils.isNotBlank(className)) {
            return camelCaseToEnglishLabel(className);
        }
        if (StringUtils.isBlank(fallback)) {
            return "";
        }
        if (containsCjk(fallback)) {
            return "";
        }
        return camelCaseToEnglishLabel(fallback);
    }

    /** ruleName -> Rule Name, ipAddress -> Ip Address, sys_user -> Sys User */
    private static String camelCaseToEnglishLabel(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        String spaced = name.trim().replaceAll("[_\\-]+", " ");
        spaced = spaced.replaceAll("([a-z])([A-Z])", "$1 $2");
        spaced = spaced.replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");
        String[] parts = spaced.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (StringUtils.isBlank(part)) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
            if (part.length() > 1) {
                sb.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return sb.toString();
    }

    private static boolean containsCjk(String text) {
        return StringUtils.isNotBlank(text) && text.codePoints().anyMatch(cp -> cp >= 0x4E00 && cp <= 0x9FFF);
    }

    private static void mergeObject(ObjectNode existing, JsonNode freshSection, String sectionName,
                                    boolean refreshStaleEnglish) {
        if (!freshSection.isObject()) {
            return;
        }
        ObjectNode target = existing.has(sectionName)
                ? (ObjectNode) existing.get(sectionName)
                : existing.putObject(sectionName);
        Iterator<String> keys = freshSection.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
            String existingText = target.path(key).asText("");
            if (shouldReplaceText(existingText, freshSection.path(key).asText(""), refreshStaleEnglish)) {
                target.set(key, freshSection.get(key));
            }
        }
    }

    private static void mergeFields(ObjectNode existing, ObjectNode fresh, boolean refreshStaleEnglish) {
        JsonNode freshFieldsNode = fresh.get("fields");
        if (freshFieldsNode == null || !freshFieldsNode.isObject()) {
            return;
        }
        ObjectNode freshFields = (ObjectNode) freshFieldsNode;
        ObjectNode existingFields = existing.has("fields")
                ? (ObjectNode) existing.get("fields")
                : existing.putObject("fields");
        Iterator<String> fieldNames = freshFields.fieldNames();
        while (fieldNames.hasNext()) {
            String field = fieldNames.next();
            ObjectNode freshField = (ObjectNode) freshFields.get(field);
            ObjectNode targetField = existingFields.has(field)
                    ? (ObjectNode) existingFields.get(field)
                    : existingFields.putObject(field);
            mergeTextField(targetField, freshField, "label", refreshStaleEnglish);
            mergeTextField(targetField, freshField, "placeholder", refreshStaleEnglish);
            mergeValidationNode(targetField, freshField, refreshStaleEnglish);
        }
    }

    private static void mergeValidationNode(ObjectNode targetField, ObjectNode freshField,
                                            boolean refreshStaleEnglish) {
        JsonNode freshValNode = freshField.get("validation");
        if (freshValNode == null || !freshValNode.isObject()) {
            return;
        }
        ObjectNode freshVal = (ObjectNode) freshValNode;
        ObjectNode targetVal = targetField.has("validation")
                ? (ObjectNode) targetField.get("validation")
                : targetField.putObject("validation");
        Iterator<String> rules = freshVal.fieldNames();
        while (rules.hasNext()) {
            String rule = rules.next();
            String existingText = targetVal.path(rule).asText("");
            String freshText = freshVal.path(rule).asText("");
            if (shouldReplaceText(existingText, freshText, refreshStaleEnglish)) {
                targetVal.set(rule, freshVal.get(rule));
            }
        }
    }

    private static void mergeMeta(ObjectNode existing, ObjectNode fresh, boolean refreshStaleEnglish) {
        JsonNode freshMetaNode = fresh.get("meta");
        if (freshMetaNode == null || !freshMetaNode.isObject()) {
            return;
        }
        ObjectNode freshMeta = (ObjectNode) freshMetaNode;
        ObjectNode existingMeta = existing.has("meta")
                ? (ObjectNode) existing.get("meta")
                : existing.putObject("meta");
        Iterator<String> keys = freshMeta.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
            if ("constraints".equals(key)) {
                mergeConstraints(existingMeta, freshMeta);
            } else if (!existingMeta.has(key)
                    || shouldReplaceText(existingMeta.path(key).asText(""), freshMeta.path(key).asText(""),
                    refreshStaleEnglish)) {
                existingMeta.set(key, freshMeta.get(key));
            }
        }
    }

    private static void mergeConstraints(ObjectNode existingMeta, ObjectNode freshMeta) {
        JsonNode freshConstraintsNode = freshMeta.get("constraints");
        if (freshConstraintsNode == null || !freshConstraintsNode.isObject()) {
            return;
        }
        ObjectNode freshConstraints = (ObjectNode) freshConstraintsNode;
        ObjectNode existingConstraints = existingMeta.has("constraints")
                ? (ObjectNode) existingMeta.get("constraints")
                : existingMeta.putObject("constraints");
        Iterator<String> fields = freshConstraints.fieldNames();
        while (fields.hasNext()) {
            String field = fields.next();
            ObjectNode freshC = (ObjectNode) freshConstraints.get(field);
            ObjectNode targetC = existingConstraints.has(field)
                    ? (ObjectNode) existingConstraints.get(field)
                    : existingConstraints.putObject(field);
            Iterator<String> rules = freshC.fieldNames();
            while (rules.hasNext()) {
                String rule = rules.next();
                if (!targetC.has(rule)) {
                    targetC.set(rule, freshC.get(rule));
                }
            }
        }
    }

    private static void mergeTextField(ObjectNode target, ObjectNode source, String key,
                                       boolean refreshStaleEnglish) {
        if (!source.has(key)) {
            return;
        }
        String existingText = target.path(key).asText("");
        String freshText = source.path(key).asText("");
        if (shouldReplaceText(existingText, freshText, refreshStaleEnglish)) {
            target.set(key, source.get(key));
        }
    }

    private static boolean shouldReplaceText(String existingText, String freshText, boolean refreshStaleEnglish) {
        if (StringUtils.isBlank(freshText)) {
            return false;
        }
        if (StringUtils.isBlank(existingText)) {
            return true;
        }
        return refreshStaleEnglish && containsCjk(existingText) && !containsCjk(freshText);
    }

    private static ObjectNode normalizeI18n(ObjectNode source) {
        return orderObject(source, List.of("page", "fields", "meta"));
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
        if ("page".equals(key)) {
            return orderObject(object, List.of(
                    "title", "description", "addTitle", "editTitle", "deleteConfirm",
                    "statusNormal", "statusDisabled"
            ));
        }
        if ("meta".equals(key)) {
            return orderObject(object, List.of("businessName", "apiModule", "functionName", "constraints"));
        }
        if (object.has("label") || object.has("placeholder") || object.has("validation")) {
            return orderObject(object, List.of("label", "placeholder", "validation"));
        }
        if ("validation".equals(key) || "constraints".equals(key) || object.has("required")
                || object.has("maxLength") || object.has("minLength") || object.has("max") || object.has("min")) {
            return orderObject(object, List.of("required", "maxLength", "minLength", "max", "min"));
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
