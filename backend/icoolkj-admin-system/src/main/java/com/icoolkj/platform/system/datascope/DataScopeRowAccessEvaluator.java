package com.icoolkj.platform.system.datascope;

import com.icoolkj.platform.common.enums.DataScopeEnum;
import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.framework.datascope.DataScopeColumns;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.system.dept.domain.DeptEntity;
import com.icoolkj.platform.system.role.domain.RoleDeptEntity;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.dept.mapper.DeptMapper;
import com.icoolkj.platform.system.role.mapper.RoleDeptMapper;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class DataScopeRowAccessEvaluator {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RoleDeptMapper roleDeptMapper;
    private final DeptMapper deptMapper;

    public DataScopeRowAccessEvaluator(UserMapper userMapper,
                                       UserRoleMapper userRoleMapper,
                                       RoleMapper roleMapper,
                                       RoleDeptMapper roleDeptMapper,
                                       DeptMapper deptMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.roleDeptMapper = roleDeptMapper;
        this.deptMapper = deptMapper;
    }

    public boolean isReadable(DataScopeSpec spec, String ownerUserId, String ownerDeptId) {
        DataScopeColumns.assertLegal(spec);
        String tenantId = SecurityContextHelper.getTenantId();
        String operatorUserId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(operatorUserId)) {
            return false;
        }
        UserEntity operator = userMapper.selectById(operatorUserId);
        if (operator == null) {
            return false;
        }
        String operatorDeptId = operator.getDeptId();
        List<String> roleIds = userRoleMapper.selectRoleIdsByTenantAndUser(tenantId, operatorUserId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return DataScopeMatchSupport.matchesSelf(
                    spec.hasUserColumn(),
                    spec.hasDeptColumn(),
                    ownerUserId,
                    ownerDeptId,
                    operatorUserId,
                    operatorDeptId);
        }
        boolean anyNormalRole = false;
        for (String roleId : roleIds) {
            RoleEntity role = roleMapper.selectById(roleId);
            if (role == null || !StatusEnum.isNormal(role.getStatus())) {
                continue;
            }
            anyNormalRole = true;
            DataScopeEnum scope = DataScopeEnum.fromCode(role.getDataScope());
            if (scope == DataScopeEnum.ALL) {
                return true;
            }
            if (matchesRole(scope, spec, ownerUserId, ownerDeptId, tenantId, operatorUserId, operatorDeptId, roleId)) {
                return true;
            }
        }
        if (!anyNormalRole) {
            return DataScopeMatchSupport.matchesSelf(
                    spec.hasUserColumn(),
                    spec.hasDeptColumn(),
                    ownerUserId,
                    ownerDeptId,
                    operatorUserId,
                    operatorDeptId);
        }
        return false;
    }

    private boolean matchesRole(DataScopeEnum scope,
                                DataScopeSpec spec,
                                String ownerUserId,
                                String ownerDeptId,
                                String tenantId,
                                String operatorUserId,
                                String operatorDeptId,
                                String roleId) {
        return switch (scope) {
            case ALL -> true;
            case SELF -> DataScopeMatchSupport.matchesSelf(
                    spec.hasUserColumn(),
                    spec.hasDeptColumn(),
                    ownerUserId,
                    ownerDeptId,
                    operatorUserId,
                    operatorDeptId);
            case DEPT -> DataScopeMatchSupport.matchesDept(
                    spec.hasUserColumn(),
                    spec.hasDeptColumn(),
                    ownerUserId,
                    ownerDeptId,
                    operatorUserId,
                    operatorDeptId);
            case DEPT_AND_CHILD -> matchesDeptAndChild(
                    spec, ownerUserId, ownerDeptId, tenantId, operatorUserId, operatorDeptId);
            case CUSTOM -> matchesCustom(
                    spec, ownerUserId, ownerDeptId, tenantId, operatorUserId, operatorDeptId, roleId);
        };
    }

    private boolean matchesDeptAndChild(DataScopeSpec spec,
                                        String ownerUserId,
                                        String ownerDeptId,
                                        String tenantId,
                                        String operatorUserId,
                                        String operatorDeptId) {
        String ancestors = null;
        if (spec.hasDeptColumn()
                && StringUtils.hasText(operatorDeptId)
                && StringUtils.hasText(ownerDeptId)
                && !ownerDeptId.equals(operatorDeptId)) {
            DeptEntity ownerDept = deptMapper.selectById(ownerDeptId);
            if (ownerDept == null || !tenantId.equals(ownerDept.getTenantId())) {
                return false;
            }
            ancestors = ownerDept.getAncestors();
        }
        return DataScopeMatchSupport.matchesDeptAndChild(
                spec.hasUserColumn(),
                spec.hasDeptColumn(),
                ownerUserId,
                ownerDeptId,
                ancestors,
                operatorUserId,
                operatorDeptId);
    }

    private boolean matchesCustom(DataScopeSpec spec,
                                  String ownerUserId,
                                  String ownerDeptId,
                                  String tenantId,
                                  String operatorUserId,
                                  String operatorDeptId,
                                  String roleId) {
        if (!spec.hasDeptColumn()) {
            return DataScopeMatchSupport.matchesSelf(
                    spec.hasUserColumn(),
                    spec.hasDeptColumn(),
                    ownerUserId,
                    ownerDeptId,
                    operatorUserId,
                    operatorDeptId);
        }
        RoleDeptEntity query = new RoleDeptEntity();
        query.setTenantId(tenantId);
        query.setRoleId(roleId);
        List<RoleDeptEntity> rows = roleDeptMapper.selectList(query);
        List<String> allowed = CollectionUtils.isEmpty(rows)
                ? List.of()
                : rows.stream().map(RoleDeptEntity::getDeptId).filter(StringUtils::hasText).distinct().toList();
        Boolean matched = DataScopeMatchSupport.matchesCustomOrFallbackSelf(ownerDeptId, allowed);
        if (matched == null) {
            return DataScopeMatchSupport.matchesSelf(
                    spec.hasUserColumn(),
                    spec.hasDeptColumn(),
                    ownerUserId,
                    ownerDeptId,
                    operatorUserId,
                    operatorDeptId);
        }
        return matched;
    }
}
