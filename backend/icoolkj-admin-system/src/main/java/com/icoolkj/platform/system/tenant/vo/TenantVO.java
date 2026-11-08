package com.icoolkj.platform.system.tenant.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 租户表列表/详情 */
@Data
public class TenantVO {
    private String id;
    private String tenantCode;
    private String tenantName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String licenseNo;
    private LocalDateTime expireAt;
    private Integer accountLimit;
    private String status;
    private String tenantType;
    private String parentTenantId;
    private String rootRegionId;
    private String rootRegionName;
    private String remark;
}
