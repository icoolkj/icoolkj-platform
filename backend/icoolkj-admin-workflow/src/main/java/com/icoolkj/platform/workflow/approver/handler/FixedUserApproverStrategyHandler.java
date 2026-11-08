package com.icoolkj.platform.workflow.approver.handler;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.approver.ApproverStrategyContext;
import com.icoolkj.platform.workflow.approver.ApproverStrategyHandler;
import com.icoolkj.platform.workflow.constant.WfApproverStrategyConfigKeys;
import com.icoolkj.platform.workflow.constant.WfApproverStrategyType;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FixedUserApproverStrategyHandler implements ApproverStrategyHandler {

    @Override
    public String strategyType() {
        return WfApproverStrategyType.FIXED_USER;
    }

    @Override
    public void validateConfig(Map<String, Object> config) {
        Object userId = config == null ? null : config.get(WfApproverStrategyConfigKeys.USER_ID);
        if (userId == null || !StringUtils.hasText(String.valueOf(userId))) {
            throw new BusinessException("error.workflow.approver_rule_invalid");
        }
    }

    @Override
    public List<String> resolveUserIds(ApproverStrategyContext context) {
        Object userId = context.config().get(WfApproverStrategyConfigKeys.USER_ID);
        if (userId == null || !StringUtils.hasText(String.valueOf(userId))) {
            return List.of();
        }
        return List.of(String.valueOf(userId));
    }

    @Override
    public String describe(Map<String, Object> config, String resolveScope) {
        Object userId = config == null ? null : config.get(WfApproverStrategyConfigKeys.USER_ID);
        return strategyType() + " / " + scope(resolveScope) + " / userId="
                + (userId == null ? "-" : userId);
    }

    private static String scope(String resolveScope) {
        return StringUtils.hasText(resolveScope) ? resolveScope.trim() : "biz_tenant";
    }
}
