package com.icoolkj.platform.system.datascope;

import com.icoolkj.platform.framework.annotation.DataScope;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

/** Builds L3 SQL fragments; values must pass ident/id whitelist. */
public final class DataScopeSqlSupport {

    private static final Pattern SAFE_IDENT = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");
    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9_\\-]{1,64}$");

    private DataScopeSqlSupport() {
    }

    public static String column(String alias, String column) {
        String col = requireIdent(column, "column");
        if (!StringUtils.hasText(alias)) {
            return col;
        }
        return requireIdent(alias, "alias") + "." + col;
    }

    public static String eqLiteral(String alias, String column, String value) {
        return column(alias, column) + " = " + quoted(value);
    }

    public static String inLiterals(String alias, String column, Collection<String> values) {
        String joined = values.stream()
                .filter(StringUtils::hasText)
                .map(DataScopeSqlSupport::quoted)
                .distinct()
                .collect(Collectors.joining(","));
        if (!StringUtils.hasText(joined)) {
            return "1 = 0";
        }
        return column(alias, column) + " IN (" + joined + ")";
    }

    public static String quoted(String value) {
        return "'" + escape(requireSafeId(value)) + "'";
    }

    public static String escape(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    public static boolean hasDeptColumn(DataScope dataScope) {
        return StringUtils.hasText(dataScope.deptColumn());
    }

    public static boolean hasUserColumn(DataScope dataScope) {
        return StringUtils.hasText(dataScope.userColumn());
    }

    public static String selfPredicate(DataScope dataScope, String userId, String deptId) {
        if (hasUserColumn(dataScope)) {
            return eqLiteral(dataScope.userAlias(), dataScope.userColumn(), userId);
        }
        if (hasDeptColumn(dataScope) && StringUtils.hasText(deptId)) {
            return eqLiteral(dataScope.deptAlias(), dataScope.deptColumn(), deptId);
        }
        return "1 = 0";
    }

    public static String deptAndChildren(DataScope dataScope, String tenantId, String deptId) {
        String deptColumn = column(dataScope.deptAlias(), dataScope.deptColumn());
        return "(" + deptColumn + " = " + quoted(deptId)
                + " OR " + deptColumn + " IN ("
                + "SELECT id FROM sys_dept WHERE tenant_id = " + quoted(tenantId)
                + " AND deleted = 0 AND FIND_IN_SET(" + quoted(deptId) + ", ancestors)"
                + "))";
    }

    private static String requireIdent(String value, String kind) {
        if (!StringUtils.hasText(value) || !SAFE_IDENT.matcher(value).matches()) {
            throw new IllegalArgumentException("Unsafe data-scope " + kind + ": " + value);
        }
        return value;
    }

    private static String requireSafeId(String value) {
        if (!StringUtils.hasText(value) || !SAFE_ID.matcher(value).matches()) {
            throw new IllegalArgumentException("Unsafe data-scope id");
        }
        return value;
    }
}
