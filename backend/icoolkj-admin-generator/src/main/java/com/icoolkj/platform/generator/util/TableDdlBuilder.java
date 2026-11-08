package com.icoolkj.platform.generator.util;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.generator.constant.GenModuleCatalog;
import com.icoolkj.platform.generator.vo.GenTableAlterDesignVO;
import com.icoolkj.platform.generator.vo.GenTableAlterModifyColumnVO;
import com.icoolkj.platform.generator.vo.GenTableDesignColumnVO;
import com.icoolkj.platform.generator.vo.GenTableDesignVO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** AI 表设计 → CREATE TABLE DDL */
public final class TableDdlBuilder {

    private static final Pattern TABLE_NAME = Pattern.compile("^(sys|infra|biz|oa)_[a-z][a-z0-9_]{0,62}$");
    private static final Pattern COLUMN_NAME = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");
    private static final Pattern CREATE_TABLE_NAME = Pattern.compile(
            "create\\s+table\\s+(?:if\\s+not\\s+exists\\s+)?`?([a-z][a-z0-9_]*)`?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ALTER_TABLE_NAME = Pattern.compile(
            "alter\\s+table\\s+`?([a-z][a-z0-9_]*)`?",
            Pattern.CASE_INSENSITIVE);
    private static final Set<String> RESERVED = Set.of(
            "id", "tenant_id", "created_by", "created_at", "updated_by", "updated_at", "deleted");

    private TableDdlBuilder() {
    }

    public static String buildCreateTableSql(GenTableDesignVO design) {
        validateDesign(design);
        GenModuleCatalog.enrichDesign(design);
        String tableName = design.getTableName().trim().toLowerCase(Locale.ROOT);
        boolean tenant = GenModuleCatalog.includeTenantColumn(design.getApiModule());

        List<String> lines = new ArrayList<>();
        lines.add("  id varchar(20) not null comment '主键ID'");
        if (tenant) {
            lines.add("  tenant_id varchar(20) not null default '0' comment '租户ID，0表示平台级'");
        }
        for (GenTableDesignColumnVO col : design.getColumns()) {
            lines.add(formatBusinessColumn(col));
        }
        lines.add("  created_by varchar(20) null comment '创建人'");
        lines.add("  created_at datetime(3) not null default current_timestamp(3) comment '创建时间'");
        lines.add("  updated_by varchar(20) null comment '更新人'");
        lines.add("  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间'");
        lines.add("  deleted tinyint(1) not null default 0 comment '是否删除'");
        lines.add("  primary key (id)");
        if (tenant) {
            lines.add("  key idx_" + tableName + "_tenant_id (tenant_id)");
        }

        String comment = escapeComment(StringUtils.defaultIfBlank(design.getTableComment(), tableName));
        return "CREATE TABLE IF NOT EXISTS " + tableName + " (\n"
                + String.join(",\n", lines)
                + "\n) engine=innodb default charset=utf8mb4 comment='" + comment + "'";
    }

    public static void validateDesign(GenTableDesignVO design) {
        if (design == null || StringUtils.isBlank(design.getTableName())) {
            throw new BusinessException("error.gen.page_spec.table_name_required");
        }
        design.setApiModule(StringUtils.defaultIfBlank(design.getApiModule(), "system"));
        design.setTableName(GenModuleCatalog.normalizeTableName(design.getApiModule(), design.getTableName()));
        GenModuleCatalog.validateTablePrefix(design);
        GenModuleCatalog.enrichDesign(design);
        String tableName = design.getTableName().trim().toLowerCase(Locale.ROOT);
        if (!TABLE_NAME.matcher(tableName).matches()) {
            throw new BusinessException("error.gen.table_design.invalid_table_name", design.getTableName());
        }
        if (design.getColumns() == null || design.getColumns().isEmpty()) {
            throw new BusinessException("error.gen.table_design.columns_required");
        }
        Set<String> names = new LinkedHashSet<>();
        for (GenTableDesignColumnVO col : design.getColumns()) {
            if (col == null || StringUtils.isBlank(col.getColumnName())) {
                throw new BusinessException("error.gen.table_design.invalid_column");
            }
            String columnName = col.getColumnName().trim().toLowerCase(Locale.ROOT);
            if (!COLUMN_NAME.matcher(columnName).matches() || RESERVED.contains(columnName)) {
                throw new BusinessException("error.gen.table_design.invalid_column", col.getColumnName());
            }
            if (!names.add(columnName)) {
                throw new BusinessException("error.gen.table_design.duplicate_column", columnName);
            }
            if (StringUtils.isBlank(col.getColumnType())) {
                throw new BusinessException("error.gen.table_design.invalid_column_type", columnName);
            }
            col.setColumnName(columnName);
        }
        design.setTableName(tableName);
    }

    public static void validateExecutableDdl(String ddl) {
        validateSafeDdl(ddl, "create table");
    }

    public static String buildAlterTableSql(GenTableAlterDesignVO alter, Set<String> existingColumns) {
        validateAlterDesign(alter, existingColumns);
        String tableName = alter.getTableName().trim().toLowerCase(Locale.ROOT);
        List<String> clauses = new ArrayList<>();
        if (alter.getAddColumns() != null) {
            for (GenTableDesignColumnVO col : alter.getAddColumns()) {
                clauses.add("ADD COLUMN " + formatBusinessColumn(col).trim());
            }
        }
        if (alter.getModifyColumns() != null) {
            for (GenTableAlterModifyColumnVO col : alter.getModifyColumns()) {
                clauses.add("MODIFY COLUMN " + formatBusinessColumn(toDesignColumn(col)).trim());
            }
        }
        if (clauses.isEmpty()) {
            throw new BusinessException("error.gen.table_design.alter_empty");
        }
        return "ALTER TABLE " + tableName + "\n  " + String.join(",\n  ", clauses);
    }

    public static void validateAlterDesign(GenTableAlterDesignVO alter, Set<String> existingColumns) {
        if (alter == null || StringUtils.isBlank(alter.getTableName())) {
            throw new BusinessException("error.gen.page_spec.table_name_required");
        }
        String tableName = alter.getTableName().trim().toLowerCase(Locale.ROOT);
        if (!TABLE_NAME.matcher(tableName).matches()) {
            throw new BusinessException("error.gen.table_design.invalid_table_name", alter.getTableName());
        }
        alter.setTableName(tableName);
        Set<String> existing = existingColumns == null ? Set.of() : existingColumns;
        Set<String> pending = new LinkedHashSet<>(existing);

        if (alter.getAddColumns() != null) {
            for (GenTableDesignColumnVO col : alter.getAddColumns()) {
                validateBusinessColumn(col);
                if (existing.contains(col.getColumnName())) {
                    throw new BusinessException("error.gen.table_design.column_exists", col.getColumnName());
                }
                if (!pending.add(col.getColumnName())) {
                    throw new BusinessException("error.gen.table_design.duplicate_column", col.getColumnName());
                }
            }
        }
        if (alter.getModifyColumns() != null) {
            for (GenTableAlterModifyColumnVO col : alter.getModifyColumns()) {
                if (col == null || StringUtils.isBlank(col.getColumnName())) {
                    throw new BusinessException("error.gen.table_design.invalid_column");
                }
                String columnName = col.getColumnName().trim().toLowerCase(Locale.ROOT);
                if (!COLUMN_NAME.matcher(columnName).matches() || RESERVED.contains(columnName)) {
                    throw new BusinessException("error.gen.table_design.alter_system_column", col.getColumnName());
                }
                if (!existing.contains(columnName)) {
                    throw new BusinessException("error.gen.table_design.column_not_exists", columnName);
                }
                if (StringUtils.isBlank(col.getColumnType())) {
                    throw new BusinessException("error.gen.table_design.invalid_column_type", columnName);
                }
                col.setColumnName(columnName);
                col.setColumnType(col.getColumnType().trim().toLowerCase(Locale.ROOT));
            }
        }
        boolean hasAdd = alter.getAddColumns() != null && !alter.getAddColumns().isEmpty();
        boolean hasModify = alter.getModifyColumns() != null && !alter.getModifyColumns().isEmpty();
        if (!hasAdd && !hasModify) {
            throw new BusinessException("error.gen.table_design.alter_empty");
        }
    }

    public static void validateExecutableAlterDdl(String ddl) {
        validateSafeDdl(ddl, "alter table");
    }

    public static List<String> splitStatements(String ddl) {
        if (StringUtils.isBlank(ddl)) {
            throw new BusinessException("error.gen.table_design.invalid_ddl");
        }
        List<String> statements = new ArrayList<>();
        for (String part : ddl.split(";")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                statements.add(trimmed);
            }
        }
        if (statements.isEmpty()) {
            throw new BusinessException("error.gen.table_design.invalid_ddl");
        }
        return statements;
    }

    public static void validateExecutableStatement(String statement) {
        String normalized = statement.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("create table")) {
            validateSafeDdl(statement, "create table");
            extractCreateTableName(statement);
            return;
        }
        if (normalized.startsWith("alter table")) {
            validateSafeDdl(statement, "alter table");
            extractAlterTableName(statement);
            return;
        }
        throw new BusinessException("error.gen.table_design.invalid_ddl");
    }

    public static String extractCreateTableName(String ddl) {
        Matcher matcher = CREATE_TABLE_NAME.matcher(ddl.trim());
        if (!matcher.find()) {
            throw new BusinessException("error.gen.table_design.invalid_ddl");
        }
        return normalizeTableName(matcher.group(1));
    }

    public static String extractAlterTableName(String ddl) {
        Matcher matcher = ALTER_TABLE_NAME.matcher(ddl.trim());
        if (!matcher.find()) {
            throw new BusinessException("error.gen.table_design.invalid_ddl");
        }
        return normalizeTableName(matcher.group(1));
    }

    private static String normalizeTableName(String tableName) {
        String name = tableName.trim().toLowerCase(Locale.ROOT);
        if (!TABLE_NAME.matcher(name).matches()) {
            throw new BusinessException("error.gen.table_design.invalid_table_name", name);
        }
        return name;
    }

    public static boolean isSystemColumn(String columnName) {
        return StringUtils.isBlank(columnName) || RESERVED.contains(columnName.trim().toLowerCase(Locale.ROOT));
    }

    private static void validateSafeDdl(String ddl, String requiredPrefix) {
        if (StringUtils.isBlank(ddl)) {
            throw new BusinessException("error.gen.table_design.invalid_ddl");
        }
        String normalized = ddl.trim().toLowerCase(Locale.ROOT);
        if (!normalized.startsWith(requiredPrefix)) {
            throw new BusinessException("error.gen.table_design.invalid_ddl");
        }
        if (normalized.contains(";") && normalized.indexOf(';') < normalized.length() - 1) {
            throw new BusinessException("error.gen.table_design.invalid_ddl");
        }
        for (String forbidden : List.of(" drop ", " truncate ", " insert ", " update ", " delete ", " grant ", " rename ")) {
            if (normalized.contains(forbidden)) {
                throw new BusinessException("error.gen.table_design.invalid_ddl");
            }
        }
        if ("alter table".equals(requiredPrefix) && normalized.contains("create table")) {
            throw new BusinessException("error.gen.table_design.invalid_ddl");
        }
        if ("create table".equals(requiredPrefix) && normalized.contains(" alter ")) {
            throw new BusinessException("error.gen.table_design.invalid_ddl");
        }
    }

    private static void validateBusinessColumn(GenTableDesignColumnVO col) {
        if (col == null || StringUtils.isBlank(col.getColumnName())) {
            throw new BusinessException("error.gen.table_design.invalid_column");
        }
        String columnName = col.getColumnName().trim().toLowerCase(Locale.ROOT);
        if (!COLUMN_NAME.matcher(columnName).matches() || RESERVED.contains(columnName)) {
            throw new BusinessException("error.gen.table_design.invalid_column", col.getColumnName());
        }
        if (StringUtils.isBlank(col.getColumnType())) {
            throw new BusinessException("error.gen.table_design.invalid_column_type", columnName);
        }
        col.setColumnName(columnName);
        col.setColumnType(col.getColumnType().trim().toLowerCase(Locale.ROOT));
    }

    private static GenTableDesignColumnVO toDesignColumn(GenTableAlterModifyColumnVO col) {
        GenTableDesignColumnVO design = new GenTableDesignColumnVO();
        design.setColumnName(col.getColumnName());
        design.setColumnComment(col.getColumnComment());
        design.setColumnType(col.getColumnType());
        design.setRequired(col.getRequired());
        return design;
    }

    private static String formatBusinessColumn(GenTableDesignColumnVO col) {
        String type = col.getColumnType().trim().toLowerCase(Locale.ROOT);
        if (!type.matches("^(varchar\\(\\d+\\)|char\\(\\d+\\)|int|bigint|tinyint\\(\\d+\\)|decimal\\(\\d+,\\d+\\)|datetime\\(3\\)|text)$")) {
            throw new BusinessException("error.gen.table_design.invalid_column_type", col.getColumnName());
        }
        boolean required = Boolean.TRUE.equals(col.getRequired());
        String nullSql = required ? "not null" : "null";
        String comment = escapeComment(StringUtils.defaultIfBlank(col.getColumnComment(), col.getColumnName()));
        return "  " + col.getColumnName() + " " + type + " " + nullSql + " comment '" + comment + "'";
    }

    private static String escapeComment(String comment) {
        return comment.replace("'", "''");
    }
}