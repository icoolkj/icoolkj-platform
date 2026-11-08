package com.icoolkj.platform.infra.fileaccess;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.util.StringUtils;

/**
 * 文件访问调用方上下文。
 */
public record FileAccessCaller(
        boolean authenticated,
        String userId,
        String tenantId,
        Set<String> permissions,
        FileAccessPurpose purpose
) {

    public FileAccessCaller {
        permissions = permissions == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(permissions));
        purpose = purpose == null ? FileAccessPurpose.DOWNLOAD : purpose;
    }

    public boolean isPlatformOperator() {
        return authenticated && TenantConstants.PLATFORM_TENANT_ID.equals(tenantId);
    }

    public static FileAccessCaller fromSecurityContext(FileAccessPurpose purpose) {
        boolean authenticated = SecurityContextHelper.isAuthenticated();
        if (!authenticated) {
            return anonymous(purpose);
        }
        Collection<String> perms = SecurityContextHelper.getPermissions();
        Set<String> permissionSet = perms == null || perms.isEmpty()
                ? Set.of()
                : Set.copyOf(perms);
        return new FileAccessCaller(
                true,
                SecurityContextHelper.getUserId(),
                SecurityContextHelper.getTenantId(),
                permissionSet,
                purpose);
    }

    public static FileAccessCaller anonymous(FileAccessPurpose purpose) {
        return new FileAccessCaller(false, null, null, Set.of(), purpose);
    }

    public boolean hasAnyPermission(Set<String> requiredAnyOf) {
        if (requiredAnyOf == null || requiredAnyOf.isEmpty()) {
            return true;
        }
        if (!authenticated || permissions.isEmpty()) {
            return false;
        }
        for (String code : requiredAnyOf) {
            if (StringUtils.hasText(code) && permissions.contains(code.trim())) {
                return true;
            }
        }
        return false;
    }
}
