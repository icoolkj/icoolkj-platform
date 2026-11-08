package com.icoolkj.platform.system.banner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 轮播图状态切换 */
@Data
public class BannerStatusUpdateRequest {
    @NotBlank(message = "{validation.id.required}")
    private String id;
    @NotBlank(message = "{gen.banner.status.required}")
    @Size(max = 1, message = "{gen.banner.status.maxLength}")
    private String status;
}
