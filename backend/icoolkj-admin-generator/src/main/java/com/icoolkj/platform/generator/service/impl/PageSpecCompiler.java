package com.icoolkj.platform.generator.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.generator.constant.GenConstants;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenTableColumn;
import com.icoolkj.platform.generator.mapper.GenTableMapper;
import com.icoolkj.platform.generator.service.GenTableService;
import com.icoolkj.platform.generator.util.GenViewBuilder;
import com.icoolkj.platform.generator.util.VelocityUtils;
import com.icoolkj.platform.generator.vo.GenPageSpecCompileResultVO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * page-spec.yaml → gen_table / gen_table_column → generateCode
 */
@Service
public class PageSpecCompiler {

    private final GenTableMapper genTableMapper;
    private final GenTableService genTableService;

    public PageSpecCompiler(GenTableMapper genTableMapper, GenTableService genTableService) {
        this.genTableMapper = genTableMapper;
        this.genTableService = genTableService;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public GenPageSpecCompileResultVO compile(String yamlContent) {
        Map<String, Object> doc = parseYaml(yamlContent);
        String layout = stringVal(doc.get("layout"));
        if ("master-detail".equals(layout)) {
            throw new BusinessException("error.gen.page_spec.master_detail_unsupported");
        }
        if (!List.of("standard-crud", "tree-crud").contains(layout)) {
            throw new BusinessException("error.gen.page_spec.layout_unsupported", layout);
        }

        Map<String, Object> entity = mapVal(doc.get("entity"));
        if (entity == null) {
            throw new BusinessException("error.gen.page_spec.entity_required");
        }
        String tableName = stringVal(entity.get("tableName"));
        if (StringUtils.isBlank(tableName)) {
            throw new BusinessException("error.gen.page_spec.table_name_required");
        }

        String pageKey = stringVal(doc.get("pageKey"));
        Map<String, Object> meta = mapVal(doc.get("meta"));
        List<Map<String, Object>> fields = listOfMaps(doc.get("fields"));

        ensureImported(tableName);
        GenTable table = loadTable(tableName);

        applyTableMeta(table, pageKey, layout, entity, meta);
        applyFieldSpecs(table.getColumns(), fields);

        genTableService.updateGenTable(table);
        genTableService.generateCode(tableName);
        mergeExtensionsIntoViewJson(table, mapVal(doc.get("extensions")));

        GenPageSpecCompileResultVO result = new GenPageSpecCompileResultVO();
        result.setTableName(tableName);
        result.setPageKey(pageKey);
        result.setTableId(table.getId());
        return result;
    }

    /**
     * Write page-spec {@code extensions} into Git view.json (FE + BE classpath copies).
     * Later generateCode merges keep unknown top-level keys including extensions.
     */
    private void mergeExtensionsIntoViewJson(GenTable table, Map<String, Object> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode extensionsNode = mapper.valueToTree(normalizeExtensions(extensions));
        for (String path : List.of(
                VelocityUtils.getGenViewPath(table),
                VelocityUtils.getGenViewBackendPath(table))) {
            try {
                File file = new File(path);
                if (!file.exists()) {
                    continue;
                }
                String existing = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                String updated = GenViewBuilder.withExtensions(existing, extensionsNode);
                FileUtils.writeStringToFile(file, updated, StandardCharsets.UTF_8);
            } catch (Exception ex) {
                throw new BusinessException("error.gen.write_file_failed", "view.json extensions");
            }
        }
    }

    /** Normalize page-spec extensions (string ids or objects) into view.json shape. */
    private static Map<String, Object> normalizeExtensions(Map<String, Object> extensions) {
        Map<String, Object> out = new HashMap<>();
        Object rowActions = extensions.get("rowActions");
        if (rowActions instanceof List<?> list) {
            List<Map<String, Object>> normalized = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String s) {
                    Map<String, Object> action = new HashMap<>();
                    action.put("id", s);
                    if ("edit".equals(s) || "delete".equals(s)) {
                        action.put("builtin", s);
                        action.put("permKey", "edit".equals(s) ? "update" : "delete");
                    } else {
                        action.put("labelKey", s);
                        action.put("permKey", "update");
                    }
                    normalized.add(action);
                } else if (item instanceof Map<?, ?> map) {
                    Map<String, Object> action = new HashMap<>();
                    map.forEach((k, v) -> action.put(String.valueOf(k), v));
                    normalized.add(action);
                }
            }
            out.put("rowActions", normalized);
        }
        Object toolbarActions = extensions.get("toolbarActions");
        if (toolbarActions instanceof List<?> list) {
            List<Map<String, Object>> normalized = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String s) {
                    Map<String, Object> action = new HashMap<>();
                    action.put("id", s);
                    if ("export".equals(s)) {
                        action.put("builtin", "export");
                        action.put("permKey", "export");
                        action.put("allowOnReadOnly", true);
                    } else if ("import".equals(s)) {
                        action.put("builtin", "import");
                        action.put("permKey", "import");
                    } else if ("importTemplate".equals(s)) {
                        // Deprecated shorthand: template lives inside import dialog
                        action.put("id", "import");
                        action.put("builtin", "import");
                        action.put("permKey", "import");
                    } else {
                        action.put("labelKey", s);
                    }
                    normalized.add(action);
                } else if (item instanceof Map<?, ?> map) {
                    Map<String, Object> action = new HashMap<>();
                    map.forEach((k, v) -> action.put(String.valueOf(k), v));
                    normalized.add(action);
                }
            }
            out.put("toolbarActions", normalized);
        }
        Object dialogs = extensions.get("dialogs");
        if (dialogs instanceof List<?> list) {
            out.put("dialogs", list.stream().map(String::valueOf).toList());
        }
        Object treeSources = extensions.get("treeSources");
        if (treeSources instanceof Map<?, ?> map) {
            Map<String, Object> normalized = new HashMap<>();
            map.forEach((k, v) -> {
                if (v instanceof Map<?, ?> cfg) {
                    Map<String, Object> entry = new HashMap<>();
                    cfg.forEach((ck, cv) -> entry.put(String.valueOf(ck), cv));
                    Object api = entry.get("api");
                    if (api != null && !entry.containsKey("apiKey")) {
                        entry.put("apiKey", String.valueOf(api));
                        entry.remove("api");
                    }
                    normalized.put(String.valueOf(k), entry);
                }
            });
            out.put("treeSources", normalized);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYaml(String yamlContent) {
        try {
            Object loaded = new Yaml().load(yamlContent);
            if (!(loaded instanceof Map)) {
                throw new BusinessException("error.gen.page_spec.yaml_root_not_mapping");
            }
            return (Map<String, Object>) loaded;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("error.gen.page_spec.invalid_yaml", ex.getMessage());
        }
    }

    private void ensureImported(String tableName) {
        GenTable existing = genTableMapper.selectGenTableByName(tableName);
        if (existing != null) {
            return;
        }
        genTableService.importTables(new String[]{tableName}, "system");
        if (genTableMapper.selectGenTableByName(tableName) == null) {
            throw new BusinessException("error.gen.page_spec.import_failed", tableName);
        }
    }

    private GenTable loadTable(String tableName) {
        GenTable table = genTableService.selectGenTableById(
                genTableMapper.selectGenTableByName(tableName).getId());
        if (table == null || table.getColumns() == null || table.getColumns().isEmpty()) {
            throw new BusinessException("error.gen.page_spec.table_not_ready", tableName);
        }
        return table;
    }

    private void applyTableMeta(GenTable table,
                                String pageKey,
                                String layout,
                                Map<String, Object> entity,
                                Map<String, Object> meta) {
        if (StringUtils.isNotBlank(pageKey)) {
            table.setBusinessName(pageKey.trim());
        }
        if (meta != null) {
            if (StringUtils.isNotBlank(stringVal(meta.get("apiModule")))) {
                table.setApiModule(stringVal(meta.get("apiModule")).trim());
            }
            if (StringUtils.isNotBlank(stringVal(meta.get("backendModule")))) {
                table.setBackendModule(stringVal(meta.get("backendModule")).trim());
            }
            if (StringUtils.isNotBlank(stringVal(meta.get("permissionPrefix")))) {
                table.setPermissionPrefix(stringVal(meta.get("permissionPrefix")).trim());
            }
            if (StringUtils.isNotBlank(stringVal(meta.get("className")))) {
                table.setClassName(stringVal(meta.get("className")).trim());
            }
        }
        if (StringUtils.isNotBlank(stringVal(entity.get("functionName")))) {
            table.setFunctionName(stringVal(entity.get("functionName")).trim());
        }
        if (StringUtils.isNotBlank(table.getApiModule())) {
            table.setRoutePrefix("/" + table.getApiModule());
        }
        table.setTplCategory("tree-crud".equals(layout) ? GenConstants.TPL_TREE : GenConstants.TPL_CRUD);
        table.setTplWebType(GenConstants.WEB_ELEMENT_PLUS_TS);
    }

    private void applyFieldSpecs(List<GenTableColumn> columns, List<Map<String, Object>> fieldSpecs) {
        if (fieldSpecs == null || fieldSpecs.isEmpty()) {
            return;
        }
        Map<String, Map<String, Object>> byField = new HashMap<>();
        for (Map<String, Object> spec : fieldSpecs) {
            String javaField = stringVal(spec.get("javaField"));
            if (StringUtils.isNotBlank(javaField)) {
                byField.put(javaField, spec);
            }
        }
        for (GenTableColumn column : columns) {
            Map<String, Object> spec = byField.get(column.getJavaField());
            if (spec == null) {
                continue;
            }
            if (spec.containsKey("columnComment")) {
                column.setColumnComment(stringVal(spec.get("columnComment")));
            }
            if (spec.containsKey("isQuery")) {
                column.setIsQuery(toFlag(spec.get("isQuery")));
            }
            if (spec.containsKey("isList")) {
                column.setIsList(toFlag(spec.get("isList")));
            }
            if (spec.containsKey("isExport")) {
                column.setIsExport(toFlag(spec.get("isExport")));
            } else if (spec.containsKey("isList") && StringUtils.isBlank(column.getIsExport())) {
                column.setIsExport(toFlag(spec.get("isList")));
            }
            if (spec.containsKey("isImport")) {
                column.setIsImport(toFlag(spec.get("isImport")));
            }
            if (spec.containsKey("isInsert")) {
                column.setIsInsert(toFlag(spec.get("isInsert")));
            }
            if (spec.containsKey("isEdit")) {
                column.setIsEdit(toFlag(spec.get("isEdit")));
            }
            if (spec.containsKey("queryType")) {
                column.setQueryType(stringVal(spec.get("queryType")));
            }
            if (spec.containsKey("htmlType")) {
                column.setHtmlType(mapHtmlType(stringVal(spec.get("htmlType"))));
            }
            if (spec.containsKey("dictType")) {
                String dictType = stringVal(spec.get("dictType"));
                column.setDictType(StringUtils.isBlank(dictType) || "null".equalsIgnoreCase(dictType) ? null : dictType);
            }
            if (Boolean.TRUE.equals(spec.get("insertOnly"))) {
                column.setIsEdit(CommonConstants.NO);
            }
        }
        reorderColumns(columns, fieldSpecs);
    }

    private void reorderColumns(List<GenTableColumn> columns, List<Map<String, Object>> fieldSpecs) {
        Map<String, GenTableColumn> columnByField = new HashMap<>();
        for (GenTableColumn column : columns) {
            columnByField.put(column.getJavaField(), column);
        }

        List<GenTableColumn> pkFirst = new ArrayList<>();
        List<GenTableColumn> userOrdered = new ArrayList<>();
        List<GenTableColumn> others = new ArrayList<>();
        List<GenTableColumn> infraTail = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Map<String, Object> spec : fieldSpecs) {
            String javaField = stringVal(spec.get("javaField"));
            GenTableColumn column = columnByField.get(javaField);
            if (column != null && seen.add(javaField)) {
                userOrdered.add(column);
            }
        }

        for (GenTableColumn column : columns) {
            String javaField = column.getJavaField();
            if (seen.contains(javaField)) {
                continue;
            }
            if (column.isPk()) {
                pkFirst.add(column);
            } else if (isInfrastructureColumn(column)) {
                infraTail.add(column);
            } else {
                others.add(column);
            }
            seen.add(javaField);
        }

        List<GenTableColumn> ordered = new ArrayList<>(pkFirst.size() + userOrdered.size() + others.size() + infraTail.size());
        ordered.addAll(pkFirst);
        ordered.addAll(userOrdered);
        ordered.addAll(others);
        ordered.addAll(infraTail);

        int sort = 1;
        for (GenTableColumn column : ordered) {
            column.setSort(sort++);
        }
        columns.clear();
        columns.addAll(ordered);
    }

    private static boolean isInfrastructureColumn(GenTableColumn column) {
        String field = column.getJavaField();
        return "tenantId".equals(field)
                || "deleted".equals(field)
                || "createdBy".equals(field)
                || "createdAt".equals(field)
                || "updatedBy".equals(field)
                || "updatedAt".equals(field);
    }

    private static String mapHtmlType(String htmlType) {
        if (StringUtils.isBlank(htmlType)) {
            return GenConstants.HTML_INPUT;
        }
        return switch (htmlType) {
            case "password", "tree-select" -> GenConstants.HTML_INPUT;
            case "tree" -> GenConstants.HTML_TREE;
            default -> htmlType;
        };
    }

    private static String toFlag(Object value) {
        if (value instanceof Boolean b) {
            return b ? GenConstants.REQUIRE : CommonConstants.NO;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0 ? GenConstants.REQUIRE : CommonConstants.NO;
        }
        String s = String.valueOf(value);
        return "true".equalsIgnoreCase(s) || GenConstants.REQUIRE.equals(s) ? GenConstants.REQUIRE : CommonConstants.NO;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapVal(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> listOfMaps(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().filter(Map.class::isInstance).map(m -> (Map<String, Object>) m).toList();
    }

    private static String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
