package com.icoolkj.platform.system.banner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新增轮播图 */
@Data
public class BannerCreateRequest {
    @NotBlank(message = "{gen.banner.bannerTitle.required}")
    @Size(max = 200, message = "{gen.banner.bannerTitle.maxLength}")
    private String bannerTitle;
    @NotBlank(message = "{gen.banner.imageFileId.required}")
    @Size(max = 20, message = "{gen.banner.imageFileId.maxLength}")
    private String imageFileId;
    @NotBlank(message = "{gen.banner.displayPositions.required}")
    @Size(max = 100, message = "{gen.banner.displayPositions.maxLength}")
    private String displayPositions;
    @Size(max = 65535, message = "{gen.banner.positionConfig.maxLength}")
    private String positionConfig;
    @NotNull(message = "{gen.banner.sortNo.required}")
    private Integer sortNo;
    @NotBlank(message = "{gen.banner.status.required}")
    @Size(max = 1, message = "{gen.banner.status.maxLength}")
    private String status;
    @Size(max = 500, message = "{gen.banner.remark.maxLength}")
    private String remark;
}
