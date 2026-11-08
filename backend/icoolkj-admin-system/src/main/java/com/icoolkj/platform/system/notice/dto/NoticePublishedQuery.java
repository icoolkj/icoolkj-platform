package com.icoolkj.platform.system.notice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 已发布公告查询 */
@Data
public class NoticePublishedQuery {

    @NotBlank
    @Size(max = 50)
    private String publishLocation;

    @Min(1)
    @Max(50)
    private Integer limit = 10;
}
