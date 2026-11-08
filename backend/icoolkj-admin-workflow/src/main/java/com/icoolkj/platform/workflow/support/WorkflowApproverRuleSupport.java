package com.icoolkj.platform.workflow.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.workflow.approver.ApproverStrategyContext;
import com.icoolkj.platform.workflow.approver.ApproverStrategyHandler;
import com.icoolkj.platform.workflow.approver.ApproverStrategyHandlerRegistry;
import com.icoolkj.platform.workflow.constant.WfApproverResolveScope;
import com.icoolkj.platform.workflow.design.bpmn.BpmnElementIds;
import com.icoolkj.platform.workflow.domain.WfApproverRuleEntity;
import com.icoolkj.platform.workflow.mapper.WfApproverRuleMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 从 wf_approver_rule 解析办理人（策略委托 {@link ApproverStrategyHandlerRegistry}） */
@Component
public class WorkflowApproverRuleSupport {

    private final WfApproverRuleMapper approverRuleMapper;
    private final WorkflowBizBindingPolicySupport bindingPolicySupport;
    private final ApproverStrategyHandlerRegistry strategyHandlerRegistry;
    private final ObjectMapper objectMapper;

    public WorkflowApproverRuleSupport(WfApproverRuleMapper approverRuleMapper,
                                       WorkflowBizBindingPolicySupport bindingPolicySupport,
                                       ApproverStrategyHandlerRegistry strategyHandlerRegistry,
                                       ObjectMapper objectMapper) {
        this.approverRuleMapper = approverRuleMapper;
        this.bindingPolicySupport = bindingPolicySupport;
        this.strategyHandlerRegistry = strategyHandlerRegistry;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> resolveStartVariables(String modelId,
                                                     String bizTenantId,
                                                     String bizType,
                                                     String starterUserId) {
        return Map.of();
    }

    /**
     * 解析单个节点的办理人列表；节点到达时调用。找不到人则抛错。
     */
    public List<String> resolveUserIdsForTask(String modelId,
                                              String taskDefinitionKey,
                                              String bizTenantId,
                                              String bizType,
                                              String starterUserId) {
        return resolveUserIdsForTask(modelId, taskDefinitionKey, bizTenantId, bizType, starterUserId, Map.of());
    }

    public List<String> resolveUserIdsForTask(String modelId,
                                              String taskDefinitionKey,
                                              String bizTenantId,
                                              String bizType,
                                              String starterUserId,
                                              Map<String, Object> processVariables) {
        if (!StringUtils.hasText(modelId) || !StringUtils.hasText(taskDefinitionKey)) {
            throw new BusinessException("error.workflow.approver_not_found", taskDefinitionKey, "-");
        }
        List<String> scopeTenantIds = new ArrayList<>();
        scopeTenantIds.add(TenantConstants.PLATFORM_TENANT_ID);
        if (bindingPolicySupport.allowTenantApproverOverride(bizType)
                && StringUtils.hasText(bizTenantId)
                && !TenantConstants.PLATFORM_TENANT_ID.equals(bizTenantId)) {
            scopeTenantIds.add(bizTenantId.trim());
        }
        List<WfApproverRuleEntity> rules = approverRuleMapper.selectByModelAndTenants(modelId, scopeTenantIds);
        if (CollectionUtils.isEmpty(rules)) {
            throw new BusinessException("error.workflow.approver_not_found", taskDefinitionKey, "no_rule");
        }
        Map<String, WfApproverRuleEntity> merged = mergeRules(rules, bizTenantId);
        WfApproverRuleEntity rule = merged.get(taskDefinitionKey);
        if (rule == null) {
            throw new BusinessException("error.workflow.approver_not_found", taskDefinitionKey, "no_rule");
        }
        List<String> userIds = resolveUserIds(rule, bizTenantId, starterUserId, processVariables);
        if (CollectionUtils.isEmpty(userIds) || !StringUtils.hasText(userIds.get(0))) {
            throw new BusinessException(
                    "error.workflow.approver_not_found",
                    describeNode(rule),
                    describeStrategy(rule));
        }
        return List.copyOf(userIds);
    }

    /** @deprecated 使用 {@link #resolveUserIdsForTask} */
    @Deprecated
    public Map<String, Object> resolveAllStartVariablesLegacy(String modelId,
                                                              String bizTenantId,
                                                              String bizType,
                                                              String starterUserId) {
        if (!StringUtils.hasText(modelId)) {
            return Map.of();
        }
        List<String> scopeTenantIds = new ArrayList<>();
        scopeTenantIds.add(TenantConstants.PLATFORM_TENANT_ID);
        if (bindingPolicySupport.allowTenantApproverOverride(bizType)
                && StringUtils.hasText(bizTenantId)
                && !TenantConstants.PLATFORM_TENANT_ID.equals(bizTenantId)) {
            scopeTenantIds.add(bizTenantId.trim());
        }
        List<WfApproverRuleEntity> rules = approverRuleMapper.selectByModelAndTenants(modelId, scopeTenantIds);
        if (CollectionUtils.isEmpty(rules)) {
            return Map.of();
        }
        Map<String, WfApproverRuleEntity> merged = mergeRules(rules, bizTenantId);
        Map<String, Object> variables = new LinkedHashMap<>();
        for (WfApproverRuleEntity rule : merged.values()) {
            List<String> userIds = resolveUserIds(rule, bizTenantId, starterUserId, Map.of());
            if (CollectionUtils.isEmpty(userIds) || !StringUtils.hasText(userIds.get(0))) {
                continue;
            }
            variables.put(rule.getVariableName(), userIds.get(0));
            String listVar = BpmnElementIds.assigneeListVar(rule.getTaskDefinitionKey());
            variables.put(listVar, List.copyOf(userIds));
        }
        return variables;
    }

    private String describeNode(WfApproverRuleEntity rule) {
        if (rule == null) {
            return "-";
        }
        return StringUtils.hasText(rule.getTaskDefinitionKey()) ? rule.getTaskDefinitionKey().trim() : "-";
    }

    private String describeStrategy(WfApproverRuleEntity rule) {
        if (rule == null) {
            return "-";
        }
        Map<String, Object> config = parseConfig(rule.getStrategyConfig());
        ApproverStrategyHandler handler = strategyHandlerRegistry.require(rule.getStrategyType());
        return handler.describe(config, rule.getResolveScope());
    }

    private Map<String, WfApproverRuleEntity> mergeRules(List<WfApproverRuleEntity> rules, String bizTenantId) {
        Map<String, WfApproverRuleEntity> platformRules = new LinkedHashMap<>();
        Map<String, WfApproverRuleEntity> tenantRules = new LinkedHashMap<>();
        for (WfApproverRuleEntity rule : rules) {
            if (TenantConstants.PLATFORM_TENANT_ID.equals(rule.getTenantId())) {
                platformRules.putIfAbsent(rule.getTaskDefinitionKey(), rule);
            } else if (StringUtils.hasText(bizTenantId) && bizTenantId.equals(rule.getTenantId())) {
                tenantRules.put(rule.getTaskDefinitionKey(), rule);
            }
        }
        Map<String, WfApproverRuleEntity> merged = new LinkedHashMap<>(platformRules);
        merged.putAll(tenantRules);
        return merged;
    }

    private List<String> resolveUserIds(WfApproverRuleEntity rule,
                                        String bizTenantId,
                                        String starterUserId,
                                        Map<String, Object> processVariables) {
        Map<String, Object> config = parseConfig(rule.getStrategyConfig());
        ApproverStrategyHandler handler = strategyHandlerRegistry.require(rule.getStrategyType());
        ApproverStrategyContext ctx = new ApproverStrategyContext(
                rule.getStrategyType(),
                config,
                rule.getResolveScope(),
                resolveLookupTenantId(rule.getResolveScope(), bizTenantId),
                starterUserId,
                processVariables == null ? Map.of() : processVariables);
        return handler.resolveUserIds(ctx);
    }

    private String resolveLookupTenantId(String resolveScope, String bizTenantId) {
        if (WfApproverResolveScope.PLATFORM_TENANT.equals(resolveScope)) {
            return TenantConstants.PLATFORM_TENANT_ID;
        }
        return StringUtils.hasText(bizTenantId) ? bizTenantId : TenantConstants.PLATFORM_TENANT_ID;
    }

    public void validateStrategyConfig(String strategyType, String strategyConfig) {
        Map<String, Object> config = parseConfig(strategyConfig);
        strategyHandlerRegistry.require(strategyType).validateConfig(config);
    }

    private Map<String, Object> parseConfig(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new BusinessException("error.workflow.approver_rule_invalid");
        }
    }
}
