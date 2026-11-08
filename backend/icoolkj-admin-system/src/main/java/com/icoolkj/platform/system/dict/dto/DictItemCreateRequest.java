package com.icoolkj.platform.system.dict.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新增字典数据项表 */
@Data
public class DictItemCreateRequest {
    @NotBlank(message = "{gen.dictItem.dictTypeId.required}")
    @Size(max = 20, message = "{gen.dictItem.dictTypeId.maxLength}")
    private String dictTypeId;
    @NotBlank(message = "{gen.dictItem.dictLabel.required}")
    @Size(max = 100, message = "{gen.dictItem.dictLabel.maxLength}")
    private String dictLabel;
    @NotBlank(message = "{gen.dictItem.dictValue.required}")
    @Size(max = 100, message = "{gen.dictItem.dictValue.maxLength}")
    private String dictValue;
    @NotBlank(message = "{gen.dictItem.valueType.required}")
    @Size(max = 20, message = "{gen.dictItem.valueType.maxLength}")
    private String valueType;
    @Size(max = 100, message = "{gen.dictItem.cssClass.maxLength}")
    private String cssClass;
    @Size(max = 64, message = "{gen.dictItem.icon.maxLength}")
    private String icon;
    @Size(max = 100, message = "{gen.dictItem.listClass.maxLength}")
    private String listClass;
    @NotBlank(message = "{gen.dictItem.isDefault.required}")
    @Size(max = 1, message = "{gen.dictItem.isDefault.maxLength}")
    private String isDefault;
    @NotNull(message = "{gen.dictItem.sortNo.required}")
    private Integer sortNo;
    @NotBlank(message = "{gen.dictItem.status.required}")
    @Size(max = 1, message = "{gen.dictItem.status.maxLength}")
    private String status;
    @Size(max = 500, message = "{gen.dictItem.remark.maxLength}")
    private String remark;
}
