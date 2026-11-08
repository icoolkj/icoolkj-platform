package com.icoolkj.platform.system.region.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 行政区划分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RegionPageQuery extends PageQuery {
    private String parentId;
    private String regionCode;
    private String regionName;
    private String regionLevel;
    private Integer sortNo;
    private String status;
}
