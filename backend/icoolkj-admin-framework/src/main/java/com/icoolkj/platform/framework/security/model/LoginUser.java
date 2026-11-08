package com.icoolkj.platform.framework.security.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Authenticated principal stored in Spring Security context and embedded in JWT claims.
 */
@Getter
@Setter
public class LoginUser implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    /** Display nickname preferred over username in UI. */
    private String nickname;
    private String password;
    private String tenantId;
    private String tenantCode;
    private String sessionId;
    private Boolean enabled = true;
    private Boolean accountNonExpired = true;
    private Boolean accountNonLocked = true;
    private Boolean credentialsNonExpired = true;
    private List<String> permissions = Collections.emptyList();
    /** Session gate: force MFA enroll after login (not a JWT claim). */
    private boolean mustEnrollMfa;

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions == null ? Collections.emptyList() : permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .filter(Objects::nonNull)
                .filter(permission -> !permission.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return Boolean.TRUE.equals(accountNonExpired);
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(accountNonLocked);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return Boolean.TRUE.equals(credentialsNonExpired);
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
