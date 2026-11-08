package com.icoolkj.platform.generator.vo;

import lombok.Data;

@Data
public class GenTableAlterModifyColumnVO {
    private String columnName;
    private String columnComment;
    private String columnType;
    private Boolean required;
}
