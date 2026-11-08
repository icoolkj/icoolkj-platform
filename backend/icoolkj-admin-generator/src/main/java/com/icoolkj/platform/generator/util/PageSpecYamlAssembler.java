package com.icoolkj.platform.generator.util;

import com.icoolkj.platform.generator.vo.GenPageSpecDraftVO;
import com.icoolkj.platform.generator.vo.GenPageSpecFieldDraftVO;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 结构化方案 → page-spec.yaml */
public final class PageSpecYamlAssembler {

    private PageSpecYamlAssembler() {
    }

    public static String toYaml(GenPageSpecDraftVO draft) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("specVersion", StringUtils.defaultIfBlank(draft.getSpecVersion(), "1.0"));
        root.put("pageKey", draft.getPageKey());
        root.put("layout", StringUtils.defaultIfBlank(draft.getLayout(), "standard-crud"));

        if (draft.getMeta() != null) {
            Map<String, Object> meta = new LinkedHashMap<>();
            put(meta, "apiModule", draft.getMeta().getApiModule());
            put(meta, "backendModule", draft.getMeta().getBackendModule());
            put(meta, "permissionPrefix", draft.getMeta().getPermissionPrefix());
            put(meta, "className", draft.getMeta().getClassName());
            root.put("meta", meta);
        }

        if (draft.getEntity() != null) {
            Map<String, Object> entity = new LinkedHashMap<>();
            put(entity, "tableName", draft.getEntity().getTableName());
            put(entity, "businessName", draft.getEntity().getBusinessName());
            put(entity, "functionName", draft.getEntity().getFunctionName());
            root.put("entity", entity);
        }

        List<Map<String, Object>> fields = new ArrayList<>();
        if (draft.getFields() != null) {
            for (GenPageSpecFieldDraftVO f : draft.getFields()) {
                Map<String, Object> item = new LinkedHashMap<>();
                put(item, "javaField", f.getJavaField());
                if (StringUtils.isNotBlank(f.getColumnComment())) {
                    item.put("columnComment", f.getColumnComment());
                }
                putBool(item, "isQuery", f.getIsQuery());
                put(item, "queryType", f.getQueryType());
                putBool(item, "isList", f.getIsList());
                putBool(item, "isExport", f.getIsExport());
                putBool(item, "isImport", f.getIsImport());
                putBool(item, "isInsert", f.getIsInsert());
                putBool(item, "isEdit", f.getIsEdit());
                put(item, "htmlType", f.getHtmlType());
                if (StringUtils.isNotBlank(f.getDictType())) {
                    item.put("dictType", f.getDictType());
                }
                fields.add(item);
            }
        }
        root.put("fields", fields);

        if (draft.getExtensions() != null && !draft.getExtensions().isEmpty()) {
            root.put("extensions", draft.getExtensions());
        }

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        return new Yaml(options).dump(root);
    }

    private static void put(Map<String, Object> map, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            map.put(key, value);
        }
    }

    private static void putBool(Map<String, Object> map, String key, Boolean value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
