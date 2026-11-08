package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GenPageSpecValidateResultVO {

    private boolean valid;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    /** 补全 meta 等后的 YAML（与输入不同时返回） */
    private String normalizedYaml;
}
