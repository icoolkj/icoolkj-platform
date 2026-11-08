package com.icoolkj.platform.system.datascope.handler;

import com.icoolkj.platform.common.enums.DataScopeEnum;
import com.icoolkj.platform.system.datascope.DataScopePredicateContext;
import com.icoolkj.platform.system.datascope.DataScopePredicateHandler;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AllDataScopeHandler implements DataScopePredicateHandler {

    @Override
    public DataScopeEnum scope() {
        return DataScopeEnum.ALL;
    }

    @Override
    public Optional<String> buildPredicate(DataScopePredicateContext context) {
        return Optional.empty();
    }
}
