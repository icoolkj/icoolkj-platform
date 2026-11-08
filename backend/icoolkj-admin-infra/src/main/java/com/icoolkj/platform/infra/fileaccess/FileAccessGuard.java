package com.icoolkj.platform.infra.fileaccess;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.FileSensitivityEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.storage.StorageProperties;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 统一文件访问守卫：按策略校验认证 / 租户范围 / 业务可见性。
 */
@Component
public class FileAccessGuard {

    private final FileAccessPolicyRegistry policyRegistry;
    private final List<FileAccessVisibilityChecker> visibilityCheckers;
    private final StorageProperties storageProperties;

    public FileAccessGuard(FileAccessPolicyRegistry policyRegistry,
                           List<FileAccessVisibilityChecker> visibilityCheckers,
                           StorageProperties storageProperties) {
        this.policyRegistry = policyRegistry;
        this.visibilityCheckers = visibilityCheckers == null ? List.of() : visibilityCheckers;
        this.storageProperties = storageProperties;
    }

    public void assertAccessible(InfraFileEntity file, FileAccessCaller caller) {
        if (file == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.file");
        }
        FileAccessCaller effectiveCaller = caller == null
                ? FileAccessCaller.fromSecurityContext(FileAccessPurpose.DOWNLOAD)
                : caller;

        // 登录页公开品牌：仅 AuthController 经 brand_pack 解析出的 fileId 会走到此 purpose。
        // 匿名一律放行，避免表策略未加载（仅剩 * / SESSION）时二维码/Logo 404。
        if (effectiveCaller.purpose() == FileAccessPurpose.LOGIN_BRAND) {
            if (!effectiveCaller.authenticated()) {
                return;
            }
            FileAccessPolicy brandPolicy = policyRegistry.resolve("brand_*");
            assertAuthMode(brandPolicy, effectiveCaller);
            assertTenantScope(brandPolicy, effectiveCaller, file);
            return;
        }

        // 公告专用入口：无论 biz_type 是否登记，强制走公告可见性（兼容历史无 biz_type 附件）
        if (effectiveCaller.purpose() == FileAccessPurpose.NOTICE_CONTENT) {
            FileAccessPolicy noticePolicy = policyRegistry.resolve("notice_*");
            assertAuthMode(noticePolicy, effectiveCaller);
            assertTenantScope(noticePolicy, effectiveCaller, file);
            assertCustomVisibility(file, effectiveCaller);
            assertSensitivity(file, effectiveCaller);
            return;
        }

        FileAccessPolicy policy = policyRegistry.resolve(file.getBizType());
        assertAuthMode(policy, effectiveCaller);
        assertTenantScope(policy, effectiveCaller, file);
        if (!effectiveCaller.hasAnyPermission(policy.permAnyOf())) {
            deny(effectiveCaller);
        }
        if (policy.visibility() == FileAccessVisibility.CUSTOM) {
            assertCustomVisibility(file, effectiveCaller);
        }
        assertSensitivity(file, effectiveCaller);
    }

    public boolean isAccessible(InfraFileEntity file, FileAccessCaller caller) {
        try {
            assertAccessible(file, caller);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }

    private void assertAuthMode(FileAccessPolicy policy, FileAccessCaller caller) {
        FileAccessAuthMode mode = policy.authMode();
        if (caller.authenticated()) {
            if (mode == FileAccessAuthMode.ANON) {
                deny(caller);
            }
            return;
        }
        // 匿名
        switch (mode) {
            case ANON, SESSION_OR_ANON -> {
                if (!policy.allowsAnonPurpose(caller.purpose())) {
                    denyUnauthorized();
                }
            }
            case SESSION -> denyUnauthorized();
        }
    }

    private void assertTenantScope(FileAccessPolicy policy,
                                   FileAccessCaller caller,
                                   InfraFileEntity file) {
        if (!caller.authenticated()) {
            // 匿名：上游已按业务绑定文件（如 login-brand 从 brand_pack 解析），不再做租户比对
            return;
        }
        String callerTenant = caller.tenantId();
        String fileTenant = file.getTenantId();
        if (!StringUtils.hasText(callerTenant)) {
            denyUnauthorized();
        }
        if (callerTenant.equals(fileTenant)) {
            return;
        }
        if (policy.tenantScope() == FileAccessTenantScope.OWN_OR_PLATFORM
                && TenantConstants.PLATFORM_TENANT_ID.equals(fileTenant)) {
            return;
        }
        deny(caller);
    }

    private void assertCustomVisibility(InfraFileEntity file, FileAccessCaller caller) {
        if (CollectionUtils.isEmpty(visibilityCheckers)) {
            deny(caller);
            return;
        }
        boolean claimed = false;
        for (FileAccessVisibilityChecker checker : visibilityCheckers) {
            if (!checker.supports(file, caller)) {
                continue;
            }
            claimed = true;
            // First claiming checker wins: deny must not fall through to a later permissive SPI.
            if (checker.isVisible(file, caller)) {
                return;
            }
            deny(caller);
            return;
        }
        // CUSTOM 无认领 checker：失败关闭
        if (!claimed) {
            deny(caller);
        }
    }

    private void assertSensitivity(InfraFileEntity file, FileAccessCaller caller) {
        StorageProperties.DownloadGuard guard = storageProperties.getSecurity().getDownloadGuard();
        if (guard == null || !guard.isEnabled() || !caller.authenticated()) {
            return;
        }
        FileSensitivityEnum min = FileSensitivityEnum.fromCode(guard.getSensitiveMinLevel());
        if (min == null) {
            min = FileSensitivityEnum.CONFIDENTIAL;
        }
        if (!FileSensitivityEnum.isAtLeast(file.getSensitivityLevel(), min)) {
            return;
        }
        String perm = StringUtils.hasText(guard.getSensitivePermission())
                ? guard.getSensitivePermission().trim()
                : "infra:file:download:sensitive";
        if (!caller.hasAnyPermission(Set.of(perm))) {
            throw new BusinessException("error.file.sensitivity_forbidden");
        }
    }

    private static void denyUnauthorized() {
        throw new BusinessException("error.auth.unauthorized");
    }

    private static void deny(FileAccessCaller caller) {
        if (caller != null && caller.purpose() == FileAccessPurpose.NOTICE_CONTENT) {
            throw new BusinessException("error.forbidden");
        }
        // 通用下载：与历史 requireFile 一致，避免枚举探测
        throw new BusinessException("error.entity.not_found", "entity.name.file");
    }
}
