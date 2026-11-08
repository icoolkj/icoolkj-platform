package com.icoolkj.platform.system.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 修改用户 — 不允许通过接口变更 userType、isSuperAdmin 及登录审计字段。 */
@Data
public class UserUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;

    @NotBlank(message = "{gen.user.deptId.required}")
    @Size(max = 20, message = "{gen.user.deptId.maxLength}")
    private String deptId;

    @NotBlank(message = "{gen.user.username.required}")
    @Size(max = 50, message = "{gen.user.username.maxLength}")
    private String username;

    @NotBlank(message = "{gen.user.nickname.required}")
    @Size(max = 50, message = "{gen.user.nickname.maxLength}")
    private String nickname;

    @Size(max = 50, message = "{gen.user.realName.maxLength}")
    private String realName;

    @Size(max = 100, message = "{gen.user.email.maxLength}")
    private String email;

    @Size(max = 20, message = "{gen.user.phone.maxLength}")
    private String phone;

    @NotBlank(message = "{gen.user.gender.required}")
    @Size(max = 1, message = "{gen.user.gender.maxLength}")
    private String gender;

    @Size(max = 255, message = "{gen.user.avatarUrl.maxLength}")
    private String avatarUrl;

    @Size(max = 20, message = "{gen.user.avatarFileId.maxLength}")
    private String avatarFileId;

    @NotBlank(message = "{gen.user.status.required}")
    @Size(max = 1, message = "{gen.user.status.maxLength}")
    private String status;

    @Size(max = 500, message = "{gen.user.remark.maxLength}")
    private String remark;
}
