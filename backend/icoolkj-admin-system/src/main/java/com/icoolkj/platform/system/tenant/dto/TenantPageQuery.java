package com.icoolkj.platform.system.tenant.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 租户表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantPageQuery extends PageQuery {
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
    private String remark;
}
