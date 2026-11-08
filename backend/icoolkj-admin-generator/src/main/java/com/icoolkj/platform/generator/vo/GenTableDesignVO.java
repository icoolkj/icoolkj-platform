package com.icoolkj.platform.generator.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GenTableDesignVO {
    private String tableName;
    private String tableComment;
    /** 前端/权限模块：system | tenant | infra | biz */
    private String apiModule;
    /** Java Maven 模块：system | infra | biz */
    private String backendModule;
    /** tenant | platform | base | plain */
    private String baseType;
    private String routePrefix;
    private String summary;
    private String ddlPreview;
    private List<GenTableDesignColumnVO> columns = new ArrayList<>();
}
