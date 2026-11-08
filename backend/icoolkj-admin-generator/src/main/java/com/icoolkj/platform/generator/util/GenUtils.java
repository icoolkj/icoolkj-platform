package com.icoolkj.platform.generator.util;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.generator.config.GenConfig;
import com.icoolkj.platform.generator.constant.GenConstants;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenTableColumn;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public final class GenUtils {

    private GenUtils() {
    }

    public static void initTable(GenTable table, String operator) {
        table.setClassName(convertClassName(table.getTableName()));
        table.setBackendModule(GenConfig.getDefaultBackendModule());
        table.setApiModule(GenConfig.getDefaultApiModule());
        table.setBaseType(GenConfig.getDefaultBaseType());
        table.setBusinessName(getBusinessName(table.getTableName()));
        table.setFunctionName(StringUtils.defaultIfBlank(table.getTableComment(), table.getClassName()));
        table.setFunctionAuthor(GenConfig.getAuthor());
        table.setRoutePrefix("/" + table.getApiModule());
        if (table.getTableName().startsWith("infra_")) {
            table.setBackendModule("infra");
            table.setApiModule("infra");
            table.setBaseType(GenConstants.BASE_PLAIN);
        } else if (table.getTableName().startsWith("biz_")) {
            table.setBackendModule("biz");
            table.setApiModule("biz");
        } else if (table.getTableName().startsWith("sys_")) {
            table.setBackendModule("system");
            table.setApiModule("system");
        } else if (table.getTableName().startsWith("oa_")) {
            table.setBackendModule("oa");
            table.setApiModule("oa");
        }
    }

    public static void initColumnField(GenTableColumn column, GenTable table) {
        String dataType = getDbType(column.getColumnType());
        String columnName = column.getColumnName();
        column.setJavaField(toCamelCase(columnName));
        column.setJavaType(GenConstants.TYPE_STRING);
        column.setQueryType(GenConstants.QUERY_EQ);
        column.setHtmlType(GenConstants.HTML_INPUT);
        column.setIsInsert(GenConstants.REQUIRE);
        column.setIsEdit(GenConstants.REQUIRE);
        column.setIsList(GenConstants.REQUIRE);
        column.setIsExport(GenConstants.REQUIRE);
        column.setIsImport(CommonConstants.NO);
        column.setIsQuery(CommonConstants.NO);
        column.setIsPk(CommonConstants.NO);

        if (Arrays.asList(GenConstants.COLUMNTYPE_STRING).contains(dataType)) {
            column.setHtmlType(GenConstants.HTML_INPUT);
        } else if (Arrays.asList(GenConstants.COLUMNTYPE_TIME).contains(dataType)) {
            column.setJavaType(GenConstants.TYPE_LOCAL_DATE_TIME);
            column.setHtmlType(GenConstants.HTML_DATETIME);
        } else if ("bigint".equals(dataType)) {
            column.setJavaType(GenConstants.TYPE_LONG);
        } else if ("decimal".equals(dataType) || "number".equals(dataType)) {
            column.setJavaType(GenConstants.TYPE_BIGDECIMAL);
        } else if (Arrays.asList(GenConstants.COLUMNTYPE_NUMBER).contains(dataType)) {
            column.setJavaType(GenConstants.TYPE_INTEGER);
        }

        if ("id".equals(columnName)) {
            column.setIsPk(GenConstants.REQUIRE);
            column.setIsInsert(CommonConstants.NO);
            column.setIsEdit(CommonConstants.NO);
            column.setIsList(CommonConstants.NO);
            column.setIsExport(CommonConstants.NO);
            column.setIsImport(CommonConstants.NO);
            column.setIsQuery(CommonConstants.NO);
        }
        if (Arrays.asList(GenConstants.COLUMN_SKIP_QUERY).contains(columnName)) {
            column.setIsQuery(CommonConstants.NO);
        } else if (!column.isPk()) {
            column.setIsQuery(GenConstants.REQUIRE);
        }
        if (StringUtils.endsWithIgnoreCase(columnName, "name") || StringUtils.endsWithIgnoreCase(columnName, "title")) {
            column.setQueryType(GenConstants.QUERY_LIKE);
        }

        if (Arrays.asList("created_by", "created_at", "updated_by", "updated_at", "deleted", "password_hash").contains(columnName)
                || isSensitiveColumn(columnName)
                || (GenConstants.BASE_TENANT.equals(table.getBaseType()) && "tenant_id".equals(columnName))) {
            column.setIsEdit(CommonConstants.NO);
            column.setIsInsert(CommonConstants.NO);
            column.setIsList(CommonConstants.NO);
            column.setIsExport(CommonConstants.NO);
            column.setIsImport(CommonConstants.NO);
            column.setIsQuery(CommonConstants.NO);
        }
        if ("avatar".equals(columnName) || StringUtils.endsWithIgnoreCase(columnName, "_avatar")) {
            column.setIsExport(CommonConstants.NO);
            column.setIsImport(CommonConstants.NO);
        }
        if ("remark".equals(columnName)) {
            column.setHtmlType(GenConstants.HTML_TEXTAREA);
        }

        if (column.getColumnLength() == null) {
            column.setColumnLength(GenI18nBuilder.parseColumnLength(column.getColumnType()));
        }
        if (!StringUtils.isNotBlank(column.getIsRequired())) {
            column.setIsRequired(CommonConstants.NO);
        }
        if (GenConstants.REQUIRE.equals(column.getIsRequired()) && column.isPk()) {
            column.setIsRequired(CommonConstants.NO);
        }
        if (StringUtils.isBlank(column.getIsExport())) {
            column.setIsExport(column.getIsList());
        }
        if (StringUtils.isBlank(column.getIsImport())) {
            column.setIsImport(CommonConstants.NO);
        }
    }

    public static String convertClassName(String tableName) {
        String name = tableName;
        for (String prefix : GenConfig.getTablePrefix().split(",")) {
            if (StringUtils.isNotBlank(prefix) && name.startsWith(prefix)) {
                name = name.substring(prefix.length());
                break;
            }
        }
        return StringUtils.capitalize(toCamelCase(name));
    }

    public static String toCamelCase(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : name.toLowerCase().toCharArray()) {
            if (c == '_') {
                upper = true;
            } else if (upper) {
                sb.append(Character.toUpperCase(c));
                upper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String getBusinessName(String tableName) {
        String name = convertClassName(tableName);
        return StringUtils.uncapitalize(name);
    }

    public static String getDbType(String columnType) {
        if (StringUtils.isBlank(columnType)) {
            return GenConstants.TYPE_STRING;
        }
        int idx = columnType.indexOf('(');
        return idx > 0 ? columnType.substring(0, idx) : columnType;
    }

    public static String toSnakeCase(String camel) {
        return RegExUtils.replaceAll(camel, "([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /** Deny-list for secrets that must never be Gen-exported/imported. */
    static boolean isSensitiveColumn(String columnName) {
        if (StringUtils.isBlank(columnName)) {
            return false;
        }
        String n = columnName.toLowerCase();
        return n.contains("password")
                || n.contains("secret")
                || n.contains("token")
                || n.contains("salt")
                || n.contains("credential")
                || "api_key".equals(n)
                || n.endsWith("_secret")
                || n.endsWith("_token");
    }
}
