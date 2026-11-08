package com.icoolkj.platform.generator.vo;

import lombok.Data;

@Data
public class GenPageSpecFieldDraftVO {
    private String javaField;
    private String columnComment;
    private Boolean isQuery;
    private String queryType;
    private Boolean isList;
    private Boolean isExport;
    private Boolean isImport;
    private Boolean isInsert;
    private Boolean isEdit;
    private String htmlType;
    private String dictType;
}
