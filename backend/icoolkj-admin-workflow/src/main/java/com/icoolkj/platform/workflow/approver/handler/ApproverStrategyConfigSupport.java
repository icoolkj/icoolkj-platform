package com.icoolkj.platform.workflow.approver.handler;

import com.icoolkj.platform.workflow.constant.WfApproverStrategyConfigKeys;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;

/** strategyConfig 读取小工具 */
final class ApproverStrategyConfigSupport {

    private ApproverStrategyConfigSupport() {
    }

    @SuppressWarnings("unchecked")
    static List<String> readRoleCodes(Map<String, Object> config) {
        if (config == null) {
            return List.of();
        }
        Object raw = config.get(WfApproverStrategyConfigKeys.ROLE_CODES);
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).filter(StringUtils::hasText).toList();
        }
        return List.of();
    }
}
