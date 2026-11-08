package com.icoolkj.platform.framework.datascope;

import com.icoolkj.platform.framework.annotation.DataScope;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

/** Rejects audit columns as L3 ({@code created_by}/{@code updated_by}). */
public final class DataScopeColumns {

    private static final Set<String> FORBIDDEN = Set.of(
            "created_by", "createdby",
            "updated_by", "updatedby"
    );

    private DataScopeColumns() {
    }

    public static void assertLegal(DataScope dataScope) {
        if (dataScope == null) {
            return;
        }
        assertLegal(dataScope.userColumn(), dataScope.deptColumn());
    }

    public static void assertLegal(DataScopeSpec spec) {
        if (spec == null) {
            return;
        }
        assertLegal(spec.userColumn(), spec.deptColumn());
    }

    public static void assertLegal(String userColumn, String deptColumn) {
        assertNotAudit("userColumn", userColumn);
        assertNotAudit("deptColumn", deptColumn);
    }

    private static void assertNotAudit(String attr, String column) {
        if (!StringUtils.hasText(column)) {
            return;
        }
        String normalized = column.trim().toLowerCase(Locale.ROOT).replace("-", "_");
        if (FORBIDDEN.contains(normalized)) {
            throw new IllegalStateException(
                    "DataScope." + attr + "='" + column.trim()
                            + "' is forbidden: audit columns must not be used for L3. "
                            + "Use user_id / dept_id (or table PK specials id).");
        }
    }
}
