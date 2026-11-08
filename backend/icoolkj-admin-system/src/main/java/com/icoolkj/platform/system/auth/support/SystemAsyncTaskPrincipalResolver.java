package com.icoolkj.platform.system.auth.support;

import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.framework.auth.PermissionService;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.infra.async.AsyncTaskPrincipalResolver;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Loads the submitting user's RBAC permissions for redis-stream / worker execution
 * so async handlers match sync request auth parity.
 */
@Component
public class SystemAsyncTaskPrincipalResolver implements AsyncTaskPrincipalResolver {

    private final UserMapper userMapper;
    private final PermissionService permissionService;

    public SystemAsyncTaskPrincipalResolver(UserMapper userMapper, PermissionService permissionService) {
        this.userMapper = userMapper;
        this.permissionService = permissionService;
    }

    @Override
    public Authentication resolve(String tenantId, String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        UserEntity user = userMapper.selectById(userId.trim());
        if (user == null) {
            return null;
        }
        String resolvedTenant = StringUtils.hasText(tenantId) ? tenantId.trim() : user.getTenantId();
        boolean superAdmin = YesNoEnum.isYes(user.getIsSuperAdmin());
        List<String> permissions = permissionService.loadPermissionCodes(
                resolvedTenant, user.getId(), superAdmin);

        LoginUser login = new LoginUser();
        login.setUserId(user.getId());
        login.setUsername(user.getUsername());
        login.setNickname(user.getNickname());
        login.setTenantId(resolvedTenant);
        if (TenantContextHolder.get() != null) {
            login.setTenantCode(TenantContextHolder.get().getTenantCode());
        }
        login.setEnabled(StatusEnum.isNormal(user.getStatus()));
        login.setAccountNonLocked(true);
        login.setAccountNonExpired(true);
        login.setCredentialsNonExpired(true);
        login.setPermissions(permissions);
        return new UsernamePasswordAuthenticationToken(login, null, login.getAuthorities());
    }
}
