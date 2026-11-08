package com.icoolkj.platform.system.tenant.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.tenant.domain.TenantEntity;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.tenant.dto.TenantAdminPageQuery;
import com.icoolkj.platform.system.tenant.mapper.TenantMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import com.icoolkj.platform.system.auth.service.LoginSecurityPolicyService;
import com.icoolkj.platform.system.user.service.PasswordHistoryService;
import com.icoolkj.platform.system.user.service.PasswordPolicyService;
import com.icoolkj.platform.system.tenant.service.TenantAdminService;
import com.icoolkj.platform.system.tenant.vo.TenantAdminVO;
import com.icoolkj.platform.system.tenant.vo.TenantProvisionVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import com.icoolkj.platform.system.auth.service.impl.LoginSecurityConfigService;

@Service
public class TenantAdminServiceImpl implements TenantAdminService {

    private final UserMapper userMapper;
    private final TenantMapper tenantMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginSecurityConfigService loginSecurityConfigService;
    private final PasswordPolicyService passwordPolicyService;
    private final PasswordHistoryService passwordHistoryService;
    private final LoginSessionService loginSessionService;
    private final LoginSecurityPolicyService loginSecurityPolicyService;

    public TenantAdminServiceImpl(UserMapper userMapper,
                                  TenantMapper tenantMapper,
                                  UserRoleMapper userRoleMapper,
                                  PasswordEncoder passwordEncoder,
                                  LoginSecurityConfigService loginSecurityConfigService,
                                  PasswordPolicyService passwordPolicyService,
                                  PasswordHistoryService passwordHistoryService,
                                  LoginSessionService loginSessionService,
                                  LoginSecurityPolicyService loginSecurityPolicyService) {
        this.userMapper = userMapper;
        this.tenantMapper = tenantMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginSecurityConfigService = loginSecurityConfigService;
        this.passwordPolicyService = passwordPolicyService;
        this.passwordHistoryService = passwordHistoryService;
        this.loginSessionService = loginSessionService;
        this.loginSecurityPolicyService = loginSecurityPolicyService;
    }

    @Override
    public PageResult<TenantAdminVO> page(TenantAdminPageQuery query) {
        long total = userMapper.countTenantAdmin(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<TenantAdminVO> list = userMapper.selectTenantAdminPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        return PageResult.of(list, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantProvisionVO resetPassword(String userId) {
        return TenantScopeRunner.runIgnoringTenant(() -> {
            UserEntity user = requireTenantSuperAdmin(userId);
            String tenantId = user.getTenantId();
            String initialPassword = loginSecurityConfigService.requireInitialPassword(tenantId);
            passwordPolicyService.validate(tenantId, initialPassword);

            String previousHash = user.getPasswordHash();
            boolean forceChange = loginSecurityConfigService.isForceChangeInitial(tenantId);
            user.setPasswordHash(passwordEncoder.encode(initialPassword));
            user.setPwdUpdatedAt(LocalDateTime.now());
            user.setMustChangePassword(forceChange ? CommonConstants.YES : CommonConstants.NO);
            user.setLoginFailCount(0);
            user.setLockedUntil(null);
            userMapper.updateById(user);
            passwordHistoryService.record(tenantId, user.getId(), previousHash);
            loginSessionService.removeAllUserSessions(tenantId, user.getId());

            TenantEntity tenant = tenantMapper.selectById(tenantId);
            TenantProvisionVO vo = new TenantProvisionVO();
            vo.setTenantId(tenantId);
            vo.setTenantCode(tenant == null ? null : tenant.getTenantCode());
            vo.setTenantName(tenant == null ? null : tenant.getTenantName());
            vo.setAdminUsername(user.getUsername());
            vo.setInitialPassword(initialPassword);
            vo.setMustChangePassword(forceChange);
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlock(String userId) {
        TenantScopeRunner.runIgnoringTenant(() -> {
            UserEntity user = requireTenantSuperAdmin(userId);
            userMapper.updateLockState(user.getId(), 0, null);
            loginSecurityPolicyService.clearLoginFailCounts(user.getTenantId(), user.getUsername());
            loginSessionService.removeAllUserSessions(user.getTenantId(), user.getId());
        });
    }

    private UserEntity requireTenantSuperAdmin(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("error.entity.not_found", "entity.name.user");
        }
        UserEntity user = userMapper.selectById(userId.trim());
        if (user == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.user");
        }
        if (!StringUtils.hasText(user.getTenantId())
                || TenantConstants.PLATFORM_TENANT_ID.equals(user.getTenantId())) {
            throw new BusinessException("error.tenant.admin_platform_forbidden");
        }
        if (!Boolean.TRUE.equals(userRoleMapper.existsSuperRoleByTenantAndUser(user.getTenantId(), user.getId()))) {
            throw new BusinessException("error.tenant.admin_not_super");
        }
        return user;
    }
}
