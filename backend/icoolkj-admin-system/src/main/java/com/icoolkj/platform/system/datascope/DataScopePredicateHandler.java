package com.icoolkj.platform.system.datascope;

import com.icoolkj.platform.common.enums.DataScopeEnum;
import java.util.Optional;

/** 单种 {@link DataScopeEnum} 对应的 SQL 谓词策略。 */
public interface DataScopePredicateHandler {

    DataScopeEnum scope();

    /**
     * @return empty = 无限制（全部数据）；否则为不含前导 AND 的谓词
     */
    Optional<String> buildPredicate(DataScopePredicateContext context);
}
