package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 结构化页面方案（向导 UI，非 YAML） */
@Data
public class GenPageSpecDraftVO {

    private String specVersion = "1.0";
    private String pageKey;
    private String layout = "standard-crud";
    /** AI 对页面意图的简短说明 */
    private String summary;
    private GenPageSpecMetaDraftVO meta = new GenPageSpecMetaDraftVO();
    private GenPageSpecEntityDraftVO entity = new GenPageSpecEntityDraftVO();
    private List<GenPageSpecFieldDraftVO> fields = new ArrayList<>();
    /**
     * 页面扩展（编译写入 view.json.extensions）。
     * 典型键：toolbarActions、rowActions、dialogs、treeSources（compile 写入 view.json.extensions）。
     */
    private Map<String, Object> extensions = new LinkedHashMap<>();
}
