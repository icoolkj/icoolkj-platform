package com.icoolkj.platform.system.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新增角色表 */
@Data
public class RoleCreateRequest {
    @NotBlank(message = "{gen.role.roleName.required}")
    @Size(max = 50, message = "{gen.role.roleName.maxLength}")
    private String roleName;
    @NotBlank(message = "{gen.role.roleCode.required}")
    @Size(max = 50, message = "{gen.role.roleCode.maxLength}")
    private String roleCode;
    @NotBlank(message = "{gen.role.dataScope.required}")
    @Size(max = 1, message = "{gen.role.dataScope.maxLength}")
    private String dataScope;
    @NotNull(message = "{gen.role.sortNo.required}")
    private Integer sortNo;
    @NotBlank(message = "{gen.role.status.required}")
    @Size(max = 1, message = "{gen.role.status.maxLength}")
    private String status;
    @NotBlank(message = "{gen.role.isSuperRole.required}")
    @Size(max = 1, message = "{gen.role.isSuperRole.maxLength}")
    private String isSuperRole;
    @Size(max = 500, message = "{gen.role.remark.maxLength}")
    private String remark;
}
