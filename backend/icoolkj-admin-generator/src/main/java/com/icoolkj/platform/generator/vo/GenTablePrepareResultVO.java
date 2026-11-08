package com.icoolkj.platform.generator.vo;

import lombok.Data;

@Data
public class GenTablePrepareResultVO {
    private String tableId;
    private String tableName;
    private String pageKey;
    /** imported | existed | created */
    private String action;
}
