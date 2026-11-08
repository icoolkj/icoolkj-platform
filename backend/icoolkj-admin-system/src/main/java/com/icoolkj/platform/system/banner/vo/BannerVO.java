package com.icoolkj.platform.system.banner.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 轮播图列表/详情 */
@Data
public class BannerVO {
    private String id;
    private String bannerTitle;
    private String imageFileId;
    private String imagePreviewUrl;
    private String displayPositions;
    private String positionConfig;
    private Integer sortNo;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}
