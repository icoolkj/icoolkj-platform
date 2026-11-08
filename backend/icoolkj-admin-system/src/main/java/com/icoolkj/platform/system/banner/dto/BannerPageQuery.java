package com.icoolkj.platform.system.banner.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 轮播图分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BannerPageQuery extends PageQuery {
    private String bannerTitle;
    private String displayPosition;
    private String status;
}
