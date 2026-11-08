package com.icoolkj.platform.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 当前用户修改密码 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "{validation.password.required}")
    @Size(max = 4096, message = "{gen.user.password.maxLength}")
    private String oldPassword;

    @NotBlank(message = "{validation.password.required}")
    @Size(max = 4096, message = "{gen.user.password.maxLength}")
    private String newPassword;
}
