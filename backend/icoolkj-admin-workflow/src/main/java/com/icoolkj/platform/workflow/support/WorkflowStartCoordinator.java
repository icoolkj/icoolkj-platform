package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.workflow.constant.WorkflowVariables;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.dto.WorkflowStartRequest;
import com.icoolkj.platform.workflow.spi.WorkflowStartContext;
import com.icoolkj.platform.workflow.spi.WorkflowStartPreparer;
import com.icoolkj.platform.workflow.spi.WorkflowUserDisplayNameResolver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 流程启动变量编排：系统保留键 → SPI preparer → 请求覆盖（保留键保护） */
@Component
public class WorkflowStartCoordinator {

    private static final Logger log = LoggerFactory.getLogger(WorkflowStartCoordinator.class);

    private final List<WorkflowStartPreparer> startPreparers;
    private final WorkflowUserDisplayNameResolver userDisplayNameResolver;

    public WorkflowStartCoordinator(List<WorkflowStartPreparer> startPreparers,
                                    WorkflowUserDisplayNameResolver userDisplayNameResolver) {
        this.startPreparers = startPreparers == null ? List.of() : startPreparers;
        this.userDisplayNameResolver = userDisplayNameResolver;
    }

    public Map<String, Object> buildStartVariables(WorkflowStartRequest request, WfModelEntity model, String starterUserId) {
        String starterDisplayName = resolveStarterDisplayName(starterUserId);
        Map<String, Object> variables = new HashMap<>();
        variables.put(WorkflowVariables.BIZ_TYPE, request.getBizType());
        variables.put(WorkflowVariables.BIZ_ID, request.getBizId());
        variables.put(WorkflowVariables.BIZ_TITLE, request.getBizTitle());
        variables.put(WorkflowVariables.TENANT_ID, request.getTenantId());
        variables.put(WorkflowVariables.STARTER_USER_ID, starterUserId);
        variables.put(WorkflowVariables.STARTER_USER_NAME, starterDisplayName);
        if (model != null && StringUtils.hasText(model.getId())) {
            variables.put(WorkflowVariables.MODEL_ID, model.getId());
        }

        WorkflowStartContext context = WorkflowStartContext.builder()
                .tenantId(request.getTenantId())
                .bizType(request.getBizType())
                .bizId(request.getBizId())
                .bizTitle(request.getBizTitle())
                .starterUserId(starterUserId)
                .starterUserName(starterDisplayName)
                .model(model)
                .build();

        for (WorkflowStartPreparer preparer : startPreparers) {
            if (preparer.supports(request.getBizType())) {
                Map<String, Object> prepared = preparer.prepareVariables(context);
                mergeNonReserved(variables, prepared, "preparer:" + preparer.getClass().getSimpleName());
            }
        }

        if (!CollectionUtils.isEmpty(request.getVariables())) {
            mergeNonReserved(variables, request.getVariables(), "request.variables");
        }
        return variables;
    }

    public static String requireBizTenantId(String tenantId) {
        if (!StringUtils.hasText(tenantId)) {
            throw new IllegalArgumentException("Workflow tenantId is required");
        }
        return tenantId.trim();
    }

    private void mergeNonReserved(Map<String, Object> target, Map<String, Object> source, String sourceLabel) {
        if (CollectionUtils.isEmpty(source)) {
            return;
        }
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.hasText(key)) {
                continue;
            }
            if (WorkflowVariables.isReserved(key)) {
                log.warn("Skip reserved workflow variable override from {}: key={}", sourceLabel, key);
                continue;
            }
            target.put(key, entry.getValue());
        }
    }

    private String resolveStarterDisplayName(String starterUserId) {
        if (StringUtils.hasText(starterUserId) && userDisplayNameResolver != null) {
            Map<String, String> names = userDisplayNameResolver.resolveDisplayNames(List.of(starterUserId));
            String fromDb = names.get(starterUserId);
            if (StringUtils.hasText(fromDb)) {
                return fromDb;
            }
        }
        String display = SecurityContextHelper.getDisplayName();
        return StringUtils.hasText(display) ? display : SecurityContextHelper.getUsername();
    }
}
