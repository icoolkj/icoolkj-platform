package com.icoolkj.platform.infra.fileaccess;

/**
 * 文件访问租户范围。
 * <p>不包含「平台运营跨租户旁路」——跨租户读必须显式使用 {@link FileAccessTenantScope#OWN_OR_PLATFORM}
 *（如品牌/公告读平台素材），或走专用 purpose 入口。
 */
public enum FileAccessTenantScope {

    /** 仅文件所属租户与调用方租户一致 */
    OWN,
    /** 本租户，或平台租户（tenant_id=0）上的文件 */
    OWN_OR_PLATFORM;

    public static FileAccessTenantScope fromCode(String code) {
        if (code == null || code.isBlank()) {
            return OWN;
        }
        return FileAccessTenantScope.valueOf(code.trim().toUpperCase());
    }
}
