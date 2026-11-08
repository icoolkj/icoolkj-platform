package com.icoolkj.platform.generator.domain;

import com.icoolkj.platform.generator.constant.GenConstants;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * gen_table_column
 */
@Data
public class GenTableColumn {

    private String id;
    private String tableId;
    private String columnName;
    private String columnComment;
    private String columnType;
    private String javaType;
    private String javaField;
    private String isPk;
    private String isInsert;
    private String isEdit;
    private String isList;
    /** Excel export allowlist (defaults aligned with isList). */
    private String isExport;
    /** Excel import allowlist (defaults aligned with isInsert). */
    private String isImport;
    private String isQuery;
    private String queryType;
    private String htmlType;
    private String dictType;
    private String isRequired;
    private Integer columnLength;
    private Integer sort;

    public boolean isPk() {
        return GenConstants.REQUIRE.equals(isPk);
    }

    public boolean isInsert() {
        return GenConstants.REQUIRE.equals(isInsert);
    }

    public boolean isEdit() {
        return GenConstants.REQUIRE.equals(isEdit);
    }

    public boolean isList() {
        return GenConstants.REQUIRE.equals(isList);
    }

    public boolean isExport() {
        return GenConstants.REQUIRE.equals(isExport);
    }

    public boolean isImport() {
        return GenConstants.REQUIRE.equals(isImport);
    }

    public boolean isQuery() {
        return GenConstants.REQUIRE.equals(isQuery);
    }

    public boolean isRequired() {
        return GenConstants.REQUIRE.equals(isRequired);
    }

    public String getCapJavaField() {
        return StringUtils.capitalize(javaField);
    }
}
