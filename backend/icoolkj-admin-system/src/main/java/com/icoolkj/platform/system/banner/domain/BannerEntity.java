package com.icoolkj.platform.system.banner.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 轮播图 — maps to `sys_banner`. */
@TableName("sys_banner")
@Getter
@Setter
public class BannerEntity extends TenantBaseEntity {

    @TableField("banner_title")
    private String bannerTitle;

    @TableField("image_file_id")
    private String imageFileId;

    @TableField("display_positions")
    private String displayPositions;

    @TableField("position_config")
    private String positionConfig;

    @TableField("sort_no")
    private Integer sortNo;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;

    @TableField(exist = false)
    private String imagePreviewUrl;
}
