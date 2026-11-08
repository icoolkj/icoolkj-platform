package com.icoolkj.platform.system.datascope.handler;

import com.icoolkj.platform.common.enums.DataScopeEnum;
import com.icoolkj.platform.system.datascope.DataScopePredicateContext;
import com.icoolkj.platform.system.datascope.DataScopePredicateHandler;
import com.icoolkj.platform.system.datascope.DataScopeSqlSupport;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DeptAndChildDataScopeHandler implements DataScopePredicateHandler {

    @Override
    public DataScopeEnum scope() {
        return DataScopeEnum.DEPT_AND_CHILD;
    }

    @Override
    public Optional<String> buildPredicate(DataScopePredicateContext context) {
        if (DataScopeSqlSupport.hasDeptColumn(context.dataScope()) && StringUtils.hasText(context.deptId())) {
            return Optional.of(DataScopeSqlSupport.deptAndChildren(
                    context.dataScope(), context.tenantId(), context.deptId()));
        }
        return Optional.of(DataScopeSqlSupport.selfPredicate(
                context.dataScope(), context.userId(), context.deptId()));
    }
}
