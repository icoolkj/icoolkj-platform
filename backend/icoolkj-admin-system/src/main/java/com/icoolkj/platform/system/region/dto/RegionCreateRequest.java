package com.icoolkj.platform.system.region.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新增行政区划 */
@Data
public class RegionCreateRequest {

    @NotBlank(message = "{gen.region.parentId.required}")
    @Size(max = 20, message = "{gen.region.parentId.maxLength}")
    private String parentId;

    @NotBlank(message = "{gen.region.regionCode.required}")
    @Size(max = 12, message = "{gen.region.regionCode.maxLength}")
    private String regionCode;

    @NotBlank(message = "{gen.region.regionName.required}")
    @Size(max = 100, message = "{gen.region.regionName.maxLength}")
    private String regionName;

    @NotBlank(message = "{gen.region.regionLevel.required}")
    @Size(max = 1, message = "{gen.region.regionLevel.maxLength}")
    private String regionLevel;

    @NotNull(message = "{gen.region.sortNo.required}")
    private Integer sortNo;

    @NotBlank(message = "{gen.region.status.required}")
    @Size(max = 1, message = "{gen.region.status.maxLength}")
    private String status;

    @Size(max = 500, message = "{gen.region.remark.maxLength}")
    private String remark;
}
