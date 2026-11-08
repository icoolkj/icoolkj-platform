package com.icoolkj.platform.system.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新增产品宇宙 */
@Data
public class ProductCreateRequest {
    @NotBlank(message = "{gen.product.code.required}")
    @Size(max = 32, message = "{gen.product.code.maxLength}")
    @Pattern(regexp = "^[a-z][a-z0-9_]{0,30}$", message = "{gen.product.code.pattern}")
    private String code;
    @NotBlank(message = "{gen.product.name.required}")
    @Size(max = 50, message = "{gen.product.name.maxLength}")
    private String name;
    @Size(max = 50, message = "{gen.product.nameEn.maxLength}")
    private String nameEn;
    @Size(max = 100, message = "{gen.product.icon.maxLength}")
    private String icon;
    @NotNull(message = "{gen.product.sortNo.required}")
    private Integer sortNo;
    @NotBlank(message = "{gen.product.status.required}")
    @Size(max = 1, message = "{gen.product.status.maxLength}")
    private String status;
    @Size(max = 500, message = "{gen.product.remark.maxLength}")
    private String remark;
}
