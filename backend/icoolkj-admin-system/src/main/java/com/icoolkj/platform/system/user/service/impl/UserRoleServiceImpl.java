package com.icoolkj.platform.system.user.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.domain.UserRoleEntity;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import com.icoolkj.platform.system.role.service.SodRoleGuard;
import com.icoolkj.platform.system.user.service.UserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final LoginSessionService loginSessionService;
    private final SodRoleGuard sodRoleGuard;

    public UserRoleServiceImpl(UserMapper userMapper,
                               UserRoleMapper userRoleMapper,
                               LoginSessionService loginSessionService,
                               SodRoleGuard sodRoleGuard) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.loginSessionService = loginSessionService;
        this.sodRoleGuard = sodRoleGuard;
    }

    @Override
    public List<String> listRoleIdsByUser(String userId) {
        UserEntity user = requireUser(userId);
        List<String> roleIds = userRoleMapper.selectRoleIdsByTenantAndUser(user.getTenantId(), userId);
        return roleIds == null ? Collections.emptyList() : roleIds;
    }

    @Override
    public void assignRoles(String userId, List<String> roleIds) {
        UserEntity user = requireUser(userId);
        String tenantId = user.getTenantId();
        sodRoleGuard.assertAssignable(tenantId, roleIds);
        UserRoleEntity deleteQuery = new UserRoleEntity();
        deleteQuery.setTenantId(tenantId);
        deleteQuery.setUserId(userId);
        userRoleMapper.delete(deleteQuery);
        if (!CollectionUtils.isEmpty(roleIds)) {
            for (String roleId : roleIds) {
                if (roleId == null || roleId.isBlank()) {
                    continue;
                }
                UserRoleEntity entity = new UserRoleEntity();
                entity.setTenantId(tenantId);
                entity.setUserId(userId);
                entity.setRoleId(roleId);
                userRoleMapper.insert(entity);
            }
        }
        loginSessionService.removeAllUserSessions(tenantId, userId);
    }

    private UserEntity requireUser(String userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.user");
        }
        return user;
    }

}
