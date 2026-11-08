package com.icoolkj.platform.system.datascope.service.impl;

import com.icoolkj.platform.common.enums.DataScopeEnum;
import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.annotation.DataScope;
import com.icoolkj.platform.framework.auth.DataPermissionService;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.system.datascope.DataScopePredicateContext;
import com.icoolkj.platform.system.datascope.DataScopePredicateHandler;
import com.icoolkj.platform.system.datascope.DataScopePredicateHandlerRegistry;
import com.icoolkj.platform.system.datascope.DataScopeRowAccessEvaluator;
import com.icoolkj.platform.system.datascope.DataScopeSqlSupport;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Primary
@Service
public class SystemDataPermissionServiceImpl implements DataPermissionService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final DataScopePredicateHandlerRegistry handlerRegistry;
    private final DataScopeRowAccessEvaluator rowAccessEvaluator;

    public SystemDataPermissionServiceImpl(UserMapper userMapper,
                                           UserRoleMapper userRoleMapper,
                                           RoleMapper roleMapper,
                                           DataScopePredicateHandlerRegistry handlerRegistry,
                                           DataScopeRowAccessEvaluator rowAccessEvaluator) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.handlerRegistry = handlerRegistry;
        this.rowAccessEvaluator = rowAccessEvaluator;
    }

    @Override
    public String buildDataScopeSql(DataScope dataScope) {
        String tenantId = SecurityContextHelper.getTenantId();
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(userId)) {
            return " AND 1 = 0";
        }

        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            return " AND 1 = 0";
        }

        List<String> roleIds = userRoleMapper.selectRoleIdsByTenantAndUser(tenantId, userId);
        String deptId = user.getDeptId();
        if (CollectionUtils.isEmpty(roleIds)) {
            return " AND " + DataScopeSqlSupport.selfPredicate(dataScope, userId, deptId);
        }

        Set<String> predicates = new LinkedHashSet<>();
        for (String roleId : roleIds) {
            RoleEntity role = roleMapper.selectById(roleId);
            if (role == null || !StatusEnum.isNormal(role.getStatus())) {
                continue;
            }
            DataScopeEnum scope = DataScopeEnum.fromCode(role.getDataScope());
            DataScopePredicateHandler handler = handlerRegistry.require(scope);
            DataScopePredicateContext ctx =
                    new DataScopePredicateContext(dataScope, tenantId, userId, deptId, roleId);
            Optional<String> predicate = handler.buildPredicate(ctx);
            if (predicate.isEmpty()) {
                return "";
            }
            predicates.add(predicate.get());
        }

        if (predicates.isEmpty()) {
            return " AND " + DataScopeSqlSupport.selfPredicate(dataScope, userId, deptId);
        }
        return " AND (" + String.join(" OR ", predicates) + ")";
    }

    @Override
    public boolean isReadable(DataScopeSpec spec, String ownerUserId, String ownerDeptId) {
        return rowAccessEvaluator.isReadable(spec, ownerUserId, ownerDeptId);
    }

    @Override
    public void assertReadable(DataScopeSpec spec, String ownerUserId, String ownerDeptId) {
        if (!isReadable(spec, ownerUserId, ownerDeptId)) {
            throw new BusinessException("error.forbidden");
        }
    }
}
