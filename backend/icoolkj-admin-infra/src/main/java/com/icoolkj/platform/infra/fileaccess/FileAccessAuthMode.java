package com.icoolkj.platform.infra.fileaccess;

/**
 * 文件访问认证模式。
 */
public enum FileAccessAuthMode {

    /** 必须已登录会话 */
    SESSION,
    /** 仅匿名（少用；一般用 SESSION_OR_ANON + anon_purposes） */
    ANON,
    /** 会话或符合策略的匿名 purpose */
    SESSION_OR_ANON;

    public static FileAccessAuthMode fromCode(String code) {
        if (code == null || code.isBlank()) {
            return SESSION;
        }
        return FileAccessAuthMode.valueOf(code.trim().toUpperCase());
    }
}
