package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GenTableDdlExecuteResultVO {
    private List<String> tableNames = new ArrayList<>();
    private List<GenTablePrepareResultVO> tables = new ArrayList<>();
}
