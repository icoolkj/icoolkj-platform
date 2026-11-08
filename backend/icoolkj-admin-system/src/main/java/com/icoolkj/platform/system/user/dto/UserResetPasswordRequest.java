package com.icoolkj.platform.system.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员重置用户密码；{@code newPassword} 留空则使用系统标准初始密码。 */
@Data
public class UserResetPasswordRequest {

    @Size(max = 4096, message = "{gen.user.password.maxLength}")
    private String newPassword;
}
