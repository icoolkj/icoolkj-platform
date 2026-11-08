package com.icoolkj.platform.generator.vo;

import lombok.Data;

@Data
public class GenTableSchemaColumnVO {
    private String columnName;
    private String columnComment;
    private String columnType;
    private Boolean required;
    private Boolean systemColumn;
}
