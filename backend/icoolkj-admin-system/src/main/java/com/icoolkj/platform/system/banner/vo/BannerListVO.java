package com.icoolkj.platform.system.banner.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 轮播图列表项 */
@Data
public class BannerListVO {
    private String id;
    private String bannerTitle;
    private String imageFileId;
    private String imagePreviewUrl;
    private String displayPositions;
    private Integer sortNo;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}
