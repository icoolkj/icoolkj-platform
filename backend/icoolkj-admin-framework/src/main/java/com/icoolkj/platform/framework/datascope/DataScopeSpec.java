package com.icoolkj.platform.framework.datascope;

import com.icoolkj.platform.framework.annotation.DataScope;
import org.springframework.util.StringUtils;

/** L3 column spec for detail assert; list filtering uses {@link DataScope}. */
public record DataScopeSpec(
        String deptAlias,
        String userAlias,
        String deptColumn,
        String userColumn
) {

    public static DataScopeSpec userOnly() {
        return new DataScopeSpec("", "", "", "user_id");
    }

    public static DataScopeSpec userAndDept() {
        return new DataScopeSpec("", "", "dept_id", "user_id");
    }

    /** {@code sys_user}: owner is primary key {@code id}. */
    public static DataScopeSpec userAsId() {
        return new DataScopeSpec("u", "u", "dept_id", "id");
    }

    /** {@code sys_dept}: scope column is primary key {@code id}. */
    public static DataScopeSpec deptAsId() {
        return new DataScopeSpec("", "", "id", "");
    }

    public static DataScopeSpec from(DataScope dataScope) {
        if (dataScope == null) {
            return userOnly();
        }
        return new DataScopeSpec(
                nullToEmpty(dataScope.deptAlias()),
                nullToEmpty(dataScope.userAlias()),
                nullToEmpty(dataScope.deptColumn()),
                nullToEmpty(dataScope.userColumn()));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public boolean hasUserColumn() {
        return StringUtils.hasText(userColumn);
    }

    public boolean hasDeptColumn() {
        return StringUtils.hasText(deptColumn);
    }
}
