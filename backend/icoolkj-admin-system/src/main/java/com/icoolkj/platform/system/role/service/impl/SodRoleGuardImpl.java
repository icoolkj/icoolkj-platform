package com.icoolkj.platform.system.role.service.impl;

import com.icoolkj.platform.common.constant.SodRoleCodes;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import com.icoolkj.platform.system.role.service.SodRoleGuard;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class SodRoleGuardImpl implements SodRoleGuard {

    private final RoleMapper roleMapper;

    public SodRoleGuardImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public void assertAssignable(String tenantId, Collection<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        Set<String> sodHits = new HashSet<>();
        for (String roleId : roleIds) {
            if (!StringUtils.hasText(roleId)) {
                continue;
            }
            RoleEntity role = roleMapper.selectById(roleId);
            if (role == null) {
                continue;
            }
            if (StringUtils.hasText(tenantId) && !tenantId.equals(role.getTenantId())) {
                continue;
            }
            if (SodRoleCodes.ALL.contains(role.getRoleCode())) {
                sodHits.add(role.getRoleCode());
            }
        }
        if (sodHits.size() > 1) {
            throw new BusinessException("error.user.sod_role_conflict");
        }
    }
}
