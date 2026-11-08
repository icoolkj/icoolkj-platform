package com.icoolkj.platform.generator.util;

import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.mapper.GenTableMapper;
import com.icoolkj.platform.generator.service.GenTableService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 补全 AI 输出中缺失的 page-spec 必填项（尤其 meta）。
 */
@Component
public class PageSpecNormalizer {

    private final GenTableMapper genTableMapper;
    private final GenTableService genTableService;

    public PageSpecNormalizer(GenTableMapper genTableMapper, GenTableService genTableService) {
        this.genTableMapper = genTableMapper;
        this.genTableService = genTableService;
    }

    public NormalizeResult normalize(String yamlContent, String tableIdHint) {
        NormalizeResult result = new NormalizeResult();
        result.setWarnings(new ArrayList<>());

        Map<String, Object> doc = parseYaml(yamlContent, result);
        if (doc == null) {
            result.setYamlContent(yamlContent);
            return result;
        }

        if (!doc.containsKey("specVersion")) {
            doc.put("specVersion", "1.0");
            result.getWarnings().add("auto-filled specVersion");
        }

        GenTable table = resolveTable(tableIdHint, doc);
        String pageKey = stringVal(doc.get("pageKey"));
        if (StringUtils.isBlank(pageKey) && table != null) {
            pageKey = table.getBusinessName();
            doc.put("pageKey", pageKey);
            result.getWarnings().add("auto-filled pageKey from gen table");
        }

        ensureMeta(doc, table, pageKey, result.getWarnings());
        ensureEntity(doc, table, pageKey, result.getWarnings());

        result.setYamlContent(dump(doc));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYaml(String yamlContent, NormalizeResult result) {
        try {
            Object loaded = new Yaml().load(yamlContent);
            if (loaded instanceof Map) {
                return new LinkedHashMap<>((Map<String, Object>) loaded);
            }
            result.getWarnings().add("YAML root is not a mapping; skip normalize");
            return null;
        } catch (Exception ex) {
            result.getWarnings().add("YAML parse failed during normalize: " + ex.getMessage());
            return null;
        }
    }

    private GenTable resolveTable(String tableIdHint, Map<String, Object> doc) {
        if (StringUtils.isNotBlank(tableIdHint)) {
            GenTable table = genTableService.selectGenTableById(tableIdHint.trim());
            if (table != null) {
                return table;
            }
        }
        Map<String, Object> entity = mapVal(doc.get("entity"));
        if (entity == null) {
            return null;
        }
        String tableName = stringVal(entity.get("tableName"));
        if (StringUtils.isBlank(tableName)) {
            return null;
        }
        GenTable row = genTableMapper.selectGenTableByName(tableName.trim());
        if (row == null) {
            return null;
        }
        return genTableService.selectGenTableById(row.getId());
    }

    @SuppressWarnings("unchecked")
    private void ensureMeta(Map<String, Object> doc,
                            GenTable table,
                            String pageKey,
                            List<String> warnings) {
        Map<String, Object> meta = mapVal(doc.get("meta"));
        boolean created = false;
        if (meta == null) {
            meta = new LinkedHashMap<>();
            doc.put("meta", meta);
            created = true;
        }

        String apiModule = pickString(meta.get("apiModule"), table != null ? table.getApiModule() : inferApiModule(doc));
        String backendModule = pickString(meta.get("backendModule"), table != null ? table.getBackendModule() : apiModule);
        String className = pickString(meta.get("className"),
                table != null ? table.getClassName() : inferClassName(pageKey, doc));
        String permissionPrefix = pickString(meta.get("permissionPrefix"),
                table != null && StringUtils.isNotBlank(table.getPermissionPrefix())
                        ? table.getPermissionPrefix()
                        : buildPermissionPrefix(apiModule, pageKey));

        putIfBlank(meta, "apiModule", apiModule);
        putIfBlank(meta, "backendModule", backendModule);
        putIfBlank(meta, "className", className);
        putIfBlank(meta, "permissionPrefix", permissionPrefix);

        if (created) {
            warnings.add("auto-filled meta block from gen table / table name");
        }
    }

    @SuppressWarnings("unchecked")
    private void ensureEntity(Map<String, Object> doc,
                              GenTable table,
                              String pageKey,
                              List<String> warnings) {
        if (table == null) {
            return;
        }
        Map<String, Object> entity = mapVal(doc.get("entity"));
        if (entity == null) {
            entity = new LinkedHashMap<>();
            doc.put("entity", entity);
            warnings.add("auto-filled entity block from gen table");
        }
        putIfBlank(entity, "tableName", table.getTableName());
        putIfBlank(entity, "businessName", StringUtils.defaultIfBlank(pageKey, table.getBusinessName()));
        putIfBlank(entity, "functionName",
                StringUtils.defaultIfBlank(table.getFunctionName(), table.getTableComment()));
    }

    private static String inferApiModule(Map<String, Object> doc) {
        Map<String, Object> entity = mapVal(doc.get("entity"));
        String tableName = entity != null ? stringVal(entity.get("tableName")) : null;
        if (StringUtils.isNotBlank(tableName)) {
            if (tableName.startsWith("infra_")) {
                return "infra";
            }
            if (tableName.startsWith("biz_")) {
                return "biz";
            }
            if (tableName.startsWith("sys_")) {
                return "system";
            }
        }
        return "system";
    }

    private static String inferClassName(String pageKey, Map<String, Object> doc) {
        Map<String, Object> entity = mapVal(doc.get("entity"));
        String tableName = entity != null ? stringVal(entity.get("tableName")) : null;
        if (StringUtils.isNotBlank(tableName)) {
            return GenUtils.convertClassName(tableName);
        }
        if (StringUtils.isNotBlank(pageKey)) {
            return StringUtils.capitalize(pageKey.trim());
        }
        return "Entity";
    }

    private static String buildPermissionPrefix(String apiModule, String pageKey) {
        String module = StringUtils.defaultIfBlank(apiModule, "system");
        String key = StringUtils.defaultIfBlank(pageKey, "entity");
        return module + ":" + key;
    }

    private static void putIfBlank(Map<String, Object> map, String key, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        Object existing = map.get(key);
        if (existing == null || (existing instanceof String s && StringUtils.isBlank(s))) {
            map.put(key, value);
        }
    }

    private static String pickString(Object existing, String fallback) {
        if (existing instanceof String s && StringUtils.isNotBlank(s)) {
            return s.trim();
        }
        return StringUtils.defaultString(fallback);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapVal(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    private static String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String dump(Map<String, Object> doc) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        return new Yaml(options).dump(doc);
    }

    public static class NormalizeResult {
        private String yamlContent;
        private List<String> warnings = new ArrayList<>();

        public String getYamlContent() {
            return yamlContent;
        }

        public void setYamlContent(String yamlContent) {
            this.yamlContent = yamlContent;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }
    }
}
