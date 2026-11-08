package com.icoolkj.platform.system.workflow;

import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.workflow.spi.WorkflowApproverUserResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/** 审批人用户解析（角色 → 用户 ID） */
@Component
public class WorkflowApproverUserResolverImpl implements WorkflowApproverUserResolver {

    private final UserMapper userMapper;

    public WorkflowApproverUserResolverImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public String resolveFirstByRoleCodes(String lookupTenantId, List<String> roleCodes) {
        List<String> userIds = resolveAllByRoleCodes(lookupTenantId, roleCodes);
        return CollectionUtils.isEmpty(userIds) ? null : userIds.get(0);
    }

    @Override
    public List<String> resolveAllByRoleCodes(String lookupTenantId, List<String> roleCodes) {
        if (CollectionUtils.isEmpty(roleCodes)) {
            return List.of();
        }
        List<String> userIds = userMapper.selectActiveUserIdsByRoleCodes(lookupTenantId, roleCodes);
        return userIds == null ? List.of() : userIds;
    }

    @Override
    public String resolveStarterDeptRole(String lookupTenantId, String starterUserId, List<String> roleCodes) {
        List<String> userIds = resolveAllStarterDeptRole(lookupTenantId, starterUserId, roleCodes);
        return CollectionUtils.isEmpty(userIds) ? null : userIds.get(0);
    }

    @Override
    public List<String> resolveAllStarterDeptRole(String lookupTenantId, String starterUserId, List<String> roleCodes) {
        if (!StringUtils.hasText(starterUserId) || CollectionUtils.isEmpty(roleCodes)) {
            return List.of();
        }
        UserEntity starter = userMapper.selectById(starterUserId);
        if (starter == null || !StringUtils.hasText(starter.getDeptId())) {
            return List.of();
        }
        List<String> userIds = userMapper.selectActiveUserIdsByRoleCodesAndDeptId(
                lookupTenantId, starter.getDeptId(), roleCodes);
        return userIds == null ? List.of() : userIds;
    }

    @Override
    public String resolveStarterUserId(String starterUserId) {
        if (!StringUtils.hasText(starterUserId)) {
            return null;
        }
        UserEntity starter = userMapper.selectById(starterUserId);
        if (starter == null || !StatusEnum.isNormal(starter.getStatus())) {
            return null;
        }
        return starter.getId();
    }
}
