package com.icoolkj.platform.system.datascope.handler;

import com.icoolkj.platform.common.enums.DataScopeEnum;
import com.icoolkj.platform.system.datascope.DataScopePredicateContext;
import com.icoolkj.platform.system.datascope.DataScopePredicateHandler;
import com.icoolkj.platform.system.datascope.DataScopeSqlSupport;
import com.icoolkj.platform.system.role.domain.RoleDeptEntity;
import com.icoolkj.platform.system.role.mapper.RoleDeptMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class CustomDeptDataScopeHandler implements DataScopePredicateHandler {

    private final RoleDeptMapper roleDeptMapper;

    public CustomDeptDataScopeHandler(RoleDeptMapper roleDeptMapper) {
        this.roleDeptMapper = roleDeptMapper;
    }

    @Override
    public DataScopeEnum scope() {
        return DataScopeEnum.CUSTOM;
    }

    @Override
    public Optional<String> buildPredicate(DataScopePredicateContext context) {
        if (!DataScopeSqlSupport.hasDeptColumn(context.dataScope())) {
            return Optional.of(DataScopeSqlSupport.selfPredicate(
                    context.dataScope(), context.userId(), context.deptId()));
        }
        RoleDeptEntity query = new RoleDeptEntity();
        query.setTenantId(context.tenantId());
        query.setRoleId(context.roleId());
        List<RoleDeptEntity> rows = roleDeptMapper.selectList(query);
        if (CollectionUtils.isEmpty(rows)) {
            return Optional.of(DataScopeSqlSupport.selfPredicate(
                    context.dataScope(), context.userId(), context.deptId()));
        }
        List<String> deptIds = rows.stream()
                .map(RoleDeptEntity::getDeptId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (deptIds.isEmpty()) {
            return Optional.of(DataScopeSqlSupport.selfPredicate(
                    context.dataScope(), context.userId(), context.deptId()));
        }
        return Optional.of(DataScopeSqlSupport.inLiterals(
                context.dataScope().deptAlias(),
                context.dataScope().deptColumn(),
                deptIds));
    }
}
