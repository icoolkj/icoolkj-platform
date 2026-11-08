package com.icoolkj.platform.workflow.approver.handler;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.approver.ApproverStrategyContext;
import com.icoolkj.platform.workflow.approver.ApproverStrategyHandler;
import com.icoolkj.platform.workflow.constant.WfApproverStrategyConfigKeys;
import com.icoolkj.platform.workflow.constant.WfApproverStrategyType;
import com.icoolkj.platform.workflow.spi.WorkflowApproverUserResolver;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RoleApproverStrategyHandler implements ApproverStrategyHandler {

    private final WorkflowApproverUserResolver approverUserResolver;

    public RoleApproverStrategyHandler(WorkflowApproverUserResolver approverUserResolver) {
        this.approverUserResolver = approverUserResolver;
    }

    @Override
    public String strategyType() {
        return WfApproverStrategyType.ROLE;
    }

    @Override
    public void validateConfig(Map<String, Object> config) {
        if (ApproverStrategyConfigSupport.readRoleCodes(config).isEmpty()) {
            throw new BusinessException("error.workflow.approver_rule_invalid");
        }
    }

    @Override
    public List<String> resolveUserIds(ApproverStrategyContext context) {
        List<String> roleCodes = ApproverStrategyConfigSupport.readRoleCodes(context.config());
        return approverUserResolver.resolveAllByRoleCodes(context.lookupTenantId(), roleCodes);
    }

    @Override
    public String describe(Map<String, Object> config, String resolveScope) {
        List<String> roleCodes = ApproverStrategyConfigSupport.readRoleCodes(config);
        return strategyType() + " / " + scope(resolveScope) + " / roles="
                + (roleCodes.isEmpty() ? "-" : String.join(",", roleCodes));
    }

    private static String scope(String resolveScope) {
        return StringUtils.hasText(resolveScope) ? resolveScope.trim() : "biz_tenant";
    }
}
