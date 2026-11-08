package com.icoolkj.platform.system.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

/** 新增租户表 */
@Data
public class TenantCreateRequest {
    @NotBlank(message = "{gen.tenant.tenantCode.required}")
    @Size(max = 50, message = "{gen.tenant.tenantCode.maxLength}")
    private String tenantCode;
    @NotBlank(message = "{gen.tenant.tenantName.required}")
    @Size(max = 100, message = "{gen.tenant.tenantName.maxLength}")
    private String tenantName;
    @Size(max = 50, message = "{gen.tenant.contactName.maxLength}")
    private String contactName;
    @Size(max = 20, message = "{gen.tenant.contactPhone.maxLength}")
    private String contactPhone;
    @Size(max = 100, message = "{gen.tenant.contactEmail.maxLength}")
    private String contactEmail;
    @Size(max = 100, message = "{gen.tenant.licenseNo.maxLength}")
    private String licenseNo;
    private LocalDateTime expireAt;
    private Integer accountLimit;
    @NotBlank(message = "{gen.tenant.status.required}")
    @Size(max = 1, message = "{gen.tenant.status.maxLength}")
    private String status;
    @NotBlank(message = "{gen.tenant.tenantType.required}")
    @Size(max = 20, message = "{gen.tenant.tenantType.maxLength}")
    private String tenantType;
    @Size(max = 20, message = "{gen.tenant.parentTenantId.maxLength}")
    private String parentTenantId;
    @Size(max = 500, message = "{gen.tenant.remark.maxLength}")
    private String remark;
    @NotBlank(message = "{gen.tenant.rootRegionId.required}")
    @Size(max = 20, message = "{gen.tenant.rootRegionId.maxLength}")
    private String rootRegionId;

    /** 开户：管理员登录名（可选，默认 tenant_admin / org_admin） */
    @Size(max = 50, message = "{gen.tenant.adminUsername.maxLength}")
    private String adminUsername;

    /** 开户：根部门编码（可选，默认 root） */
    @Size(max = 50, message = "{gen.tenant.rootDeptCode.maxLength}")
    private String rootDeptCode;

    /** 开户：根部门名称（可选，默认租户名称） */
    @Size(max = 100, message = "{gen.tenant.rootDeptName.maxLength}")
    private String rootDeptName;
}
