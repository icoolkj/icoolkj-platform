package com.icoolkj.platform.system.region.vo;

import lombok.Data;

@Data
public class RegionVO {

    private String id;
    private String parentId;
    private String ancestors;
    private String regionCode;
    private String regionName;
    private String regionLevel;
    private Integer sortNo;
    private String status;
    private String remark;
}
