package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GenTableSchemaVO {
    private String tableName;
    private String tableComment;
    private List<GenTableSchemaColumnVO> columns = new ArrayList<>();
}
