package com.icoolkj.platform.system.banner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 已发布轮播图查询 */
@Data
public class BannerPublishedQuery {
    @NotBlank(message = "{gen.banner.displayPosition.required}")
    private String displayPosition;
    private Integer limit;
}
