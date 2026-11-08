package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GenPageSpecCompileResultVO {

    private String tableName;
    private String pageKey;
    private String tableId;
    private String message;
    private List<String> warnings = new ArrayList<>();
}
