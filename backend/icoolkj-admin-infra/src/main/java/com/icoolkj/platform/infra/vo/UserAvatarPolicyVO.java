package com.icoolkj.platform.infra.vo;

import lombok.Data;

/** 头像上传策略（前端裁剪/校验） */
@Data
public class UserAvatarPolicyVO {

    private long maxSizeMb;
    private int outputSize;
    private int thumbSmSize;
    private int thumbMdSize;
}
