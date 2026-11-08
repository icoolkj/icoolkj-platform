package com.icoolkj.platform.system.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 绑定用户头像文件引用 */
@Data
public class UserAvatarBindRequest {

    @Size(max = 20, message = "{gen.user.avatarFileId.maxLength}")
    private String avatarFileId;

    @Size(max = 255, message = "{gen.user.avatarUrl.maxLength}")
    private String avatarUrl;

    @Size(max = 255, message = "{gen.user.avatarUrl.maxLength}")
    private String avatarUrlSm;

    @Size(max = 255, message = "{gen.user.avatarUrl.maxLength}")
    private String avatarUrlLg;
}
