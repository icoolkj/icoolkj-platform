package com.icoolkj.platform.infra.fileaccess;

/**
 * 下载/预览调用意图；匿名策略通过 purpose 收窄，避免任意匿名读文件。
 */
public enum FileAccessPurpose {

    /** 通用下载/预览，对应 /infra/file/{id}/download */
    DOWNLOAD,
    /** 登录页公开品牌资源，对应 /auth/login-brand 系列接口 */
    LOGIN_BRAND,
    /** 公告内容文件专用入口，对应 /system/notice/content-file/{fileId}/download */
    NOTICE_CONTENT
}
