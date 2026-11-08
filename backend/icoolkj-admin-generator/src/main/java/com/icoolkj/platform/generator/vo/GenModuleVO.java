package com.icoolkj.platform.generator.vo;

import lombok.Data;

@Data
public class GenModuleVO {
    private String apiModule;
    private String labelKey;
    private String tablePrefix;
    private String backendModule;
    private String baseType;
    private String routePrefix;
    private boolean includeTenantColumn;
}
