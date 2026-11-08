package com.icoolkj.platform.system.banner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/** 批量删除轮播图 */
@Data
public class BannerBatchDeleteRequest {
    @NotEmpty(message = "{gen.banner.ids.required}")
    private List<String> ids;
}
