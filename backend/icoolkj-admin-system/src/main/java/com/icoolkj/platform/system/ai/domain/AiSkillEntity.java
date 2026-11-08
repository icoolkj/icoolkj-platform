package com.icoolkj.platform.system.ai.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 平台 AI Skill 能力目录 — maps to {@code sys_ai_skill}（全局表，无 tenant_id）。
 */
@TableName("sys_ai_skill")
@Getter
@Setter
public class AiSkillEntity extends BaseEntity {

    @TableField("skill_code")
    private String skillCode;

    @TableField("tool_name")
    private String toolName;

    private String category;

    @TableField("skill_name")
    private String skillName;

    /** locale -> 显示名 JSON */
    @TableField("name_i18n")
    private String nameI18n;

    private String description;

    /** locale -> 描述 JSON */
    @TableField("description_i18n")
    private String descriptionI18n;

    @TableField("required_permission")
    private String requiredPermission;

    /** 模型侧工具说明（英文） */
    @TableField("tool_description")
    private String toolDescription;

    @TableField("parameters_schema_json")
    private String parametersSchemaJson;

    @TableField("sort_no")
    private Integer sortNo;

    /** 0 正常 / 1 停用 */
    private String status;

    private String remark;
}
