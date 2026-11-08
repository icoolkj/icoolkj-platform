package com.icoolkj.platform.api.ai.skill;

import com.icoolkj.platform.api.ai.skill.handler.AiSkillHandler;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.security.PermissionAuthorizationService;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.system.ai.service.AiSkillCatalogService;
import com.icoolkj.platform.system.ai.vo.AiSkillDefinitionVO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Executes office AI skills: catalog permission gate + security-context scoped handler.
 */
@Component
public class AiSkillToolExecutor {

    private final AiSkillCatalogService catalogService;
    private final AiSkillHandlerRegistry handlerRegistry;
    private final PermissionAuthorizationService authz;

    public AiSkillToolExecutor(AiSkillCatalogService catalogService,
                               AiSkillHandlerRegistry handlerRegistry,
                               PermissionAuthorizationService authz) {
        this.catalogService = catalogService;
        this.handlerRegistry = handlerRegistry;
        this.authz = authz;
    }

    public String executeTool(String toolName, String argumentsJson, Locale locale) {
        AiSkillDefinitionVO skill = catalogService.findEnabledByToolName(toolName, locale)
                .orElseThrow(() -> new BusinessException("error.ai.skill_unknown", toolName));
        if (!authz.hasPermission(skill.getRequiredPermission())) {
            throw new BusinessException("error.forbidden");
        }
        // Identity is never taken from model arguments — only from security context.
        if (!StringUtils.hasText(SecurityContextHelper.getUserId())) {
            throw new BusinessException("error.unauthorized");
        }
        AiSkillHandler handler = handlerRegistry.require(skill.getSkillCode());
        return handler.execute(argumentsJson);
    }
}
