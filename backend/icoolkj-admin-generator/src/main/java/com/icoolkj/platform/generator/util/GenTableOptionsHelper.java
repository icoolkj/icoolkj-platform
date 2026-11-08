package com.icoolkj.platform.generator.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.MenuAssignScope;
import com.icoolkj.platform.common.constant.ProductCodes;
import com.icoolkj.platform.generator.domain.GenTable;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/** 解析 gen_table.options JSON 中的菜单/产品扩展字段 */
public final class GenTableOptionsHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GenTableOptionsHelper() {
    }

    public static String getProductCode(GenTable table) {
        return StringUtils.defaultIfBlank(getString(table, "productCode"), ProductCodes.PLATFORM);
    }

    public static String getAssignScope(GenTable table) {
        String scope = getString(table, "assignScope");
        return MenuAssignScope.isPlatform(scope) ? MenuAssignScope.PLATFORM : MenuAssignScope.TENANT;
    }

    public static String getParentMenuId(GenTable table) {
        return getString(table, "parentMenuId");
    }

    public static Map<String, Object> parseOptions(GenTable table) {
        if (table == null || StringUtils.isBlank(table.getOptions())) {
            return new LinkedHashMap<>();
        }
        try {
            return MAPPER.readValue(table.getOptions(), new TypeReference<>() {});
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    public static void putOption(GenTable table, String key, Object value) {
        Map<String, Object> map = parseOptions(table);
        if (value == null || (value instanceof String s && s.isBlank())) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
        try {
            table.setOptions(map.isEmpty() ? null : MAPPER.writeValueAsString(map));
        } catch (Exception ignored) {
            table.setOptions(null);
        }
    }

    private static String getString(GenTable table, String key) {
        Object val = parseOptions(table).get(key);
        return val == null ? null : String.valueOf(val).trim();
    }
}
