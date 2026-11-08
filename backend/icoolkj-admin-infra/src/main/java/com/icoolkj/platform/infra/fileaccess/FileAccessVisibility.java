package com.icoolkj.platform.infra.fileaccess;

/**
 * 通过认证与租户范围后的业务可见性。
 */
public enum FileAccessVisibility {

    /** 无额外业务条件 */
    ALWAYS,
    /** 委托 {@link FileAccessVisibilityChecker}（如公告已发布 / 审批参与者） */
    CUSTOM;

    public static FileAccessVisibility fromCode(String code) {
        if (code == null || code.isBlank()) {
            return ALWAYS;
        }
        return FileAccessVisibility.valueOf(code.trim().toUpperCase());
    }
}
