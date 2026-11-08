package com.icoolkj.platform.generator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.icoolkj.platform.generator.constant.GenConstants;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * gen_table
 */
@Data
public class GenTable {

    private String id;
    private String tableName;
    private String tableComment;
    private String className;
    private String tplCategory = GenConstants.TPL_CRUD;
    private String tplWebType = GenConstants.WEB_ELEMENT_PLUS_TS;
    private String backendModule;
    private String apiModule;
    private String baseType;
    private String businessName;
    private String functionName;
    private String functionAuthor;
    private String routePrefix;
    private String permissionPrefix;
    private String genType = GenConstants.GEN_ZIP;
    private String genPath;
    private String options;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private GenTableColumn pkColumn;
    private List<GenTableColumn> columns;
    private Map<String, Object> params = new HashMap<>();

    /** 代码生成内部使用，REST 列表/导入预览时不序列化 */
    @JsonIgnore
    public String getEntityName() {
        if (StringUtils.isBlank(className)) {
            return null;
        }
        return className.endsWith("Entity") ? className : className + "Entity";
    }

    @JsonIgnore
    public String getPackageName() {
        if (StringUtils.isBlank(backendModule)) {
            return null;
        }
        return "com.icoolkj.platform." + backendModule;
    }

    @JsonIgnore
    public String getRoutePath() {
        if (StringUtils.isNotBlank(routePrefix) && StringUtils.isNotBlank(businessName)) {
            return routePrefix + "/" + businessName;
        }
        if (StringUtils.isNotBlank(apiModule) && StringUtils.isNotBlank(businessName)) {
            return "/" + apiModule + "/" + businessName;
        }
        return null;
    }

    public String getPermissionPrefix() {
        if (StringUtils.isNotBlank(permissionPrefix)) {
            return permissionPrefix;
        }
        if (StringUtils.isBlank(businessName)) {
            return null;
        }
        String prefix = StringUtils.defaultString(StringUtils.removeStart(routePrefix, "/")).replace("/", ":");
        if (StringUtils.isBlank(prefix)) {
            prefix = StringUtils.defaultString(apiModule);
        }
        return prefix + ":" + businessName;
    }

    public boolean crud() {
        return GenConstants.TPL_CRUD.equals(tplCategory);
    }

    public boolean isSuperColumn(String javaField) {
        if (GenConstants.BASE_PLAIN.equals(baseType)) {
            return false;
        }
        if ("id".equals(javaField) || "deleted".equals(javaField)) {
            return true;
        }
        if ("createdBy".equals(javaField) || "createdAt".equals(javaField)
                || "updatedBy".equals(javaField) || "updatedAt".equals(javaField)) {
            return true;
        }
        return GenConstants.BASE_TENANT.equals(baseType) && "tenantId".equals(javaField);
    }
}
