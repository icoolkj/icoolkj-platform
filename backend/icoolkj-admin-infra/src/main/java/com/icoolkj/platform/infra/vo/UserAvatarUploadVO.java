package com.icoolkj.platform.infra.vo;

import lombok.Data;

/** 用户头像上传结果（含多尺寸） */
@Data
public class UserAvatarUploadVO {

    private String avatarFileId;
    private String avatarUrl;
    private String avatarUrlSm;
    private String avatarUrlMd;
    private String avatarUrlLg;
}
