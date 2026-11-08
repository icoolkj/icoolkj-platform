package com.icoolkj.platform.framework.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * SpEL permission helper for {@code @PreAuthorize("@authz.hasPermission('...')")}.
 */
@Service("authz")
public class PermissionAuthorizationService {

    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> permission.equals(granted.getAuthority()));
    }

    public boolean hasAnyPermission(String... permissions) {
        if (permissions == null) {
            return false;
        }
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
