package com.icoolkj.platform.system.config.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.system.config.service.ConfigService;
import com.icoolkj.platform.workflow.spi.WorkflowNotifyTemplateSource;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 从 {@code sys_config.workflow.notify.templates} 读取事件文案模板。
 * JSON 示例：
 * <pre>
 * {
 *   "REJECT": { "title": "{eventLabel}：{bizTitle}", "content": "{title}（实例 {instanceId}）" }
 * }
 * </pre>
 */
@Primary
@Component
public class SysConfigWorkflowNotifyTemplateSource implements WorkflowNotifyTemplateSource {

    private final ConfigService configService;
    private final ObjectMapper objectMapper;

    public SysConfigWorkflowNotifyTemplateSource(ConfigService configService, ObjectMapper objectMapper) {
        this.configService = configService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<EventTemplate> find(String eventType) {
        if (!StringUtils.hasText(eventType)) {
            return Optional.empty();
        }
        String tenantId = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            tenantId = TenantConstants.PLATFORM_TENANT_ID;
        }
        String json = configService.getString(tenantId, ConfigKeys.WORKFLOW_NOTIFY_TEMPLATES, "");
        if (!StringUtils.hasText(json)) {
            return Optional.empty();
        }
        try {
            Map<String, Map<String, String>> root = objectMapper.readValue(json, new TypeReference<>() {
            });
            Map<String, String> node = root.get(eventType.trim());
            if (node == null || node.isEmpty()) {
                return Optional.empty();
            }
            String title = node.get("title");
            String content = node.get("content");
            if (!StringUtils.hasText(title) && !StringUtils.hasText(content)) {
                return Optional.empty();
            }
            return Optional.of(new EventTemplate(title, content));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
