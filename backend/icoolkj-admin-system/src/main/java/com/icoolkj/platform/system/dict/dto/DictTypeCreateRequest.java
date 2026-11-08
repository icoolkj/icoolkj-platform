package com.icoolkj.platform.system.dict.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新增字典类型表 */
@Data
public class DictTypeCreateRequest {
    @NotBlank(message = "{gen.dictType.dictName.required}")
    @Size(max = 100, message = "{gen.dictType.dictName.maxLength}")
    private String dictName;
    @NotBlank(message = "{gen.dictType.dictType.required}")
    @Size(max = 100, message = "{gen.dictType.dictType.maxLength}")
    private String dictType;
    @NotBlank(message = "{gen.dictType.status.required}")
    @Size(max = 1, message = "{gen.dictType.status.maxLength}")
    private String status;
    @Size(max = 500, message = "{gen.dictType.remark.maxLength}")
    private String remark;
}
