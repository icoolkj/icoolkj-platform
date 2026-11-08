package com.icoolkj.platform.generator.vo;

import lombok.Data;

@Data
public class GenTableDesignColumnVO {
    private String columnName;
    private String columnComment;
    private String columnType;
    private Boolean required;
    private String dictType;
}
