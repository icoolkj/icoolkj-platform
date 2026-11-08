package com.icoolkj.platform.system.tenant.domain;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 租户表 — maps to `sys_tenant`. */
@TableName("sys_tenant")
@Getter
@Setter
public class TenantEntity extends TenantBaseEntity {

    @TableField("tenant_code")
    private String tenantCode;

    @TableField("tenant_name")
    private String tenantName;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("contact_email")
    private String contactEmail;

    @TableField("license_no")
    private String licenseNo;

    @TableField("expire_at")
    private LocalDateTime expireAt;

    @TableField("account_limit")
    private Integer accountLimit;

    private String status;

    @TableField("tenant_type")
    private String tenantType;

    @TableField("parent_tenant_id")
    private String parentTenantId;

    private String ancestors;

    @TableField("root_region_id")
    private String rootRegionId;

    private String remark;

}
