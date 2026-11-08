package com.icoolkj.platform.system.banner.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 已发布轮播图（前台展示） */
@Data
public class BannerPublishedVO {
    private String id;
    private String bannerTitle;
    private String imageFileId;
    private String imagePreviewUrl;
    private String displayPositions;
    private String positionConfig;
    private Integer sortNo;
    private LocalDateTime createdAt;
}
