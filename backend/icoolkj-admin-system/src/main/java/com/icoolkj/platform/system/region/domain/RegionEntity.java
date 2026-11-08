package com.icoolkj.platform.system.region.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 行政区划 — maps to {@code sys_region}. */
@TableName("sys_region")
@Getter
@Setter
public class RegionEntity extends BaseEntity {

    @TableField("tenant_id")
    private String tenantId;

    @TableField("parent_id")
    private String parentId;

    private String ancestors;

    @TableField("region_code")
    private String regionCode;

    @TableField("region_name")
    private String regionName;

    @TableField("region_level")
    private String regionLevel;

    @TableField("sort_no")
    private Integer sortNo;

    private String status;

    private String remark;
}
