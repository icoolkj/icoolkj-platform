package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GenTableAlterDesignVO {
    private String tableName;
    private String summary;
    private String ddlPreview;
    private List<GenTableDesignColumnVO> addColumns = new ArrayList<>();
    private List<GenTableAlterModifyColumnVO> modifyColumns = new ArrayList<>();
}
