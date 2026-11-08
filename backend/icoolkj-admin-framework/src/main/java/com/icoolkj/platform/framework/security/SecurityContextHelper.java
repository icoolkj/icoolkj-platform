package com.icoolkj.platform.framework.security;

import com.icoolkj.platform.framework.security.model.LoginUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;

/**
 * Reads the current authenticated user from Spring Security context.
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {
    }

    public static String getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUsername();
    }

    public static String getNickname() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getNickname();
    }

    /** Nickname if present, otherwise username. */
    public static String getDisplayName() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return null;
        }
        String nickname = loginUser.getNickname();
        if (nickname != null && !nickname.isBlank()) {
            return nickname.trim();
        }
        return loginUser.getUsername();
    }

    public static String getTenantId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getTenantId();
    }

    public static Collection<String> getPermissions() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? Collections.emptyList() : loginUser.getPermissions();
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }
}
