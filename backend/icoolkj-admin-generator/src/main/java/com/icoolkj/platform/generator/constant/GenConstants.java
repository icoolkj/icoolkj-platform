package com.icoolkj.platform.generator.constant;

import com.icoolkj.platform.common.constant.CommonConstants;

/**
 * Code generation constants.
 */
public final class GenConstants {

    private GenConstants() {
    }

    public static final String TPL_CRUD = "crud";
    public static final String TPL_TREE = "tree";

    public static final String WEB_ELEMENT_PLUS_TS = "element-plus-ts";

    public static final String BASE_TENANT = "tenant";
    public static final String BASE_PLATFORM = "platform";
    public static final String BASE_BASE = "base";
    public static final String BASE_PLAIN = "plain";

    public static final String GEN_ZIP = "0";
    public static final String GEN_PATH = "1";

    /** Alias of {@link CommonConstants#YES} for generator column flags. */
    public static final String REQUIRE = CommonConstants.YES;

    public static final String QUERY_EQ = "EQ";
    public static final String QUERY_LIKE = "LIKE";

    public static final String HTML_INPUT = "input";
    public static final String HTML_TEXTAREA = "textarea";
    public static final String HTML_SELECT = "select";
    public static final String HTML_RADIO = "radio";
    public static final String HTML_DATETIME = "datetime";
    public static final String HTML_TREE = "tree";

    public static final String TYPE_STRING = "String";
    public static final String TYPE_INTEGER = "Integer";
    public static final String TYPE_LONG = "Long";
    public static final String TYPE_BIGDECIMAL = "BigDecimal";
    public static final String TYPE_LOCAL_DATE_TIME = "LocalDateTime";
    public static final String TYPE_LOCAL_DATE = "LocalDate";

    public static final String[] COLUMN_SKIP_ENTITY = {
            "id", "tenant_id", "created_by", "created_at", "updated_by", "updated_at", "deleted"
    };

    public static final String[] COLUMN_SKIP_QUERY = {
            "id", "created_by", "updated_by", "deleted", "password_hash"
    };

    public static final String[] COLUMNTYPE_STRING = {"char", "varchar", "nvarchar", "varchar2", "tinytext", "text", "mediumtext", "longtext"};
    public static final String[] COLUMNTYPE_TIME = {"datetime", "time", "date", "timestamp"};
    public static final String[] COLUMNTYPE_NUMBER = {"tinyint", "smallint", "mediumint", "int", "number", "integer", "bigint", "float", "double", "decimal"};
}
