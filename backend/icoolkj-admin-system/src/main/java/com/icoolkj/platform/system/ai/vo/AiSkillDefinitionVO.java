package com.icoolkj.platform.system.ai.vo;

/**
 * Resolved AI skill definition for runtime catalog / tool-calling.
 */
public class AiSkillDefinitionVO {

    private String id;
    private String skillCode;
    private String toolName;
    private String category;
    private String name;
    private String description;
    private String requiredPermission;
    private String toolDescription;
    private String parametersSchemaJson;
    private Integer sortNo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSkillCode() {
        return skillCode;
    }

    public void setSkillCode(String skillCode) {
        this.skillCode = skillCode;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public String getToolDescription() {
        return toolDescription;
    }

    public void setToolDescription(String toolDescription) {
        this.toolDescription = toolDescription;
    }

    public String getParametersSchemaJson() {
        return parametersSchemaJson;
    }

    public void setParametersSchemaJson(String parametersSchemaJson) {
        this.parametersSchemaJson = parametersSchemaJson;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
