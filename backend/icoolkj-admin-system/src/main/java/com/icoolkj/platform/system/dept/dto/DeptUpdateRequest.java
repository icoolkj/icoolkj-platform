package com.icoolkj.platform.system.dept.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 修改部门表 */
@Data
public class DeptUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;
    @NotBlank(message = "{gen.dept.parentId.required}")
    @Size(max = 20, message = "{gen.dept.parentId.maxLength}")
    private String parentId;
    @NotBlank(message = "{gen.dept.deptName.required}")
    @Size(max = 100, message = "{gen.dept.deptName.maxLength}")
    private String deptName;
    @Size(max = 50, message = "{gen.dept.deptCode.maxLength}")
    private String deptCode;
    @Size(max = 50, message = "{gen.dept.leaderName.maxLength}")
    private String leaderName;
    @Size(max = 20, message = "{gen.dept.phone.maxLength}")
    private String phone;
    @Size(max = 100, message = "{gen.dept.email.maxLength}")
    private String email;
    @NotNull(message = "{gen.dept.sortNo.required}")
    private Integer sortNo;
    @NotBlank(message = "{gen.dept.status.required}")
    @Size(max = 1, message = "{gen.dept.status.maxLength}")
    private String status;
    @Size(max = 500, message = "{gen.dept.remark.maxLength}")
    private String remark;
}
