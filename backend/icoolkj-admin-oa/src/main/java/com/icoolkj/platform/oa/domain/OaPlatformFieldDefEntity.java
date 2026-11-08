package com.icoolkj.platform.oa.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

@TableName("oa_platform_field_def")
@Getter
@Setter
public class OaPlatformFieldDefEntity extends TenantBaseEntity {

    @TableField("template_version_id")
    private String templateVersionId;

    @TableField("field_code")
    private String fieldCode;

    @TableField("field_name")
    private String fieldName;

    @TableField("field_alias")
    private String fieldAlias;

    @TableField("field_type")
    private String fieldType;

    @TableField("component_type")
    private String componentType;

    @TableField("dict_type")
    private String dictType;

    @TableField("field_required")
    private String fieldRequired;

    @TableField("sort_no")
    private Integer sortNo;

    @TableField("field_group")
    private String fieldGroup;

    @TableField("col_span")
    private Integer colSpan;

    @TableField("default_value")
    private String defaultValue;

    @TableField("max_length")
    private Integer maxLength;

    @TableField("platform_locked")
    private String platformLocked;

    @TableField("used_in_condition")
    private String usedInCondition;

    @TableField("used_in_list")
    private String usedInList;

    @TableField("promote_slot")
    private String promoteSlot;

    @TableField("list_slot_sync")
    private String listSlotSync;

    private String status;
    private String remark;
}
