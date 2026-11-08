package com.icoolkj.platform.system.datascope.handler;

import com.icoolkj.platform.common.enums.DataScopeEnum;
import com.icoolkj.platform.system.datascope.DataScopePredicateContext;
import com.icoolkj.platform.system.datascope.DataScopePredicateHandler;
import com.icoolkj.platform.system.datascope.DataScopeSqlSupport;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SelfDataScopeHandler implements DataScopePredicateHandler {

    @Override
    public DataScopeEnum scope() {
        return DataScopeEnum.SELF;
    }

    @Override
    public Optional<String> buildPredicate(DataScopePredicateContext context) {
        return Optional.of(DataScopeSqlSupport.selfPredicate(
                context.dataScope(), context.userId(), context.deptId()));
    }
}
