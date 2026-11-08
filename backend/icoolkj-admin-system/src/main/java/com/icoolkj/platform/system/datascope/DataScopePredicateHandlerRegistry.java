package com.icoolkj.platform.system.datascope;

import com.icoolkj.platform.common.enums.DataScopeEnum;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class DataScopePredicateHandlerRegistry {

    private final Map<DataScopeEnum, DataScopePredicateHandler> handlers;

    public DataScopePredicateHandlerRegistry(List<DataScopePredicateHandler> handlerList) {
        Map<DataScopeEnum, DataScopePredicateHandler> map = new EnumMap<>(DataScopeEnum.class);
        if (handlerList != null) {
            for (DataScopePredicateHandler handler : handlerList) {
                map.put(handler.scope(), handler);
            }
        }
        Set<DataScopeEnum> missing = EnumSet.allOf(DataScopeEnum.class);
        missing.removeAll(map.keySet());
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Missing DataScopePredicateHandler for scopes: " + missing);
        }
        this.handlers = Map.copyOf(map);
    }

    public DataScopePredicateHandler require(DataScopeEnum scope) {
        DataScopePredicateHandler handler = handlers.get(scope);
        if (handler == null) {
            throw new IllegalStateException("No DataScopePredicateHandler for scope: " + scope);
        }
        return handler;
    }
}
