package com.icoolkj.platform.system.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 当前用户更新个人资料（不含账号、部门、状态等管理字段） */
@Data
public class UserProfileUpdateRequest {

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
}
