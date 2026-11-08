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

/**
 * 表达式审批人白名单：
 * <ul>
 *   <li>{@code starter}</li>
 *   <li>{@code variable:&lt;processVar&gt;} — 取流程变量中的用户 id</li>
 *   <li>{@code userId:&lt;literal&gt;} — 固定用户 id</li>
 * </ul>
 */
@Component
public class ExpressionApproverStrategyHandler implements ApproverStrategyHandler {

    private final WorkflowApproverUserResolver approverUserResolver;

    public ExpressionApproverStrategyHandler(WorkflowApproverUserResolver approverUserResolver) {
        this.approverUserResolver = approverUserResolver;
    }

    @Override
    public String strategyType() {
        return WfApproverStrategyType.EXPRESSION;
    }

    @Override
    public void validateConfig(Map<String, Object> config) {
        String expr = rawExpr(config);
        if (!StringUtils.hasText(expr)) {
            throw new BusinessException("error.workflow.approver_rule_invalid");
        }
        if (!isSupported(expr)) {
            throw new BusinessException("error.workflow.approver_expression_unsupported");
        }
    }

    @Override
    public List<String> resolveUserIds(ApproverStrategyContext context) {
        String expr = rawExpr(context.config());
        if (!StringUtils.hasText(expr)) {
            return List.of();
        }
        String trimmed = expr.trim();
        if (WfApproverStrategyConfigKeys.EXPR_STARTER.equals(trimmed)) {
            String uid = approverUserResolver.resolveStarterUserId(context.starterUserId());
            return StringUtils.hasText(uid) ? List.of(uid) : List.of();
        }
        if (trimmed.startsWith(WfApproverStrategyConfigKeys.EXPR_USER_ID_PREFIX)) {
            String uid = trimmed.substring(WfApproverStrategyConfigKeys.EXPR_USER_ID_PREFIX.length()).trim();
            return StringUtils.hasText(uid) ? List.of(uid) : List.of();
        }
        if (trimmed.startsWith(WfApproverStrategyConfigKeys.EXPR_VARIABLE_PREFIX)) {
            String varName = trimmed.substring(WfApproverStrategyConfigKeys.EXPR_VARIABLE_PREFIX.length()).trim();
            if (!StringUtils.hasText(varName) || context.processVariables() == null) {
                return List.of();
            }
            Object val = context.processVariables().get(varName);
            if (val == null) {
                return List.of();
            }
            String uid = String.valueOf(val).trim();
            return StringUtils.hasText(uid) ? List.of(uid) : List.of();
        }
        throw new BusinessException("error.workflow.approver_expression_unsupported");
    }

    @Override
    public String describe(Map<String, Object> config, String resolveScope) {
        Object expr = config == null ? null : config.get(WfApproverStrategyConfigKeys.EXPR);
        return strategyType() + " / " + scope(resolveScope) + " / expr="
                + (expr == null ? "-" : expr);
    }

    private static boolean isSupported(String expr) {
        String trimmed = expr.trim();
        if (WfApproverStrategyConfigKeys.EXPR_STARTER.equals(trimmed)) {
            return true;
        }
        if (trimmed.startsWith(WfApproverStrategyConfigKeys.EXPR_USER_ID_PREFIX)) {
            return StringUtils.hasText(trimmed.substring(
                    WfApproverStrategyConfigKeys.EXPR_USER_ID_PREFIX.length()).trim());
        }
        if (trimmed.startsWith(WfApproverStrategyConfigKeys.EXPR_VARIABLE_PREFIX)) {
            return StringUtils.hasText(trimmed.substring(
                    WfApproverStrategyConfigKeys.EXPR_VARIABLE_PREFIX.length()).trim());
        }
        return false;
    }

    private static String rawExpr(Map<String, Object> config) {
        Object expr = config == null ? null : config.get(WfApproverStrategyConfigKeys.EXPR);
        return expr == null ? null : String.valueOf(expr);
    }

    private static String scope(String resolveScope) {
        return StringUtils.hasText(resolveScope) ? resolveScope.trim() : "biz_tenant";
    }
}
