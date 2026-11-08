package com.icoolkj.platform.api.ai.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.icoolkj.platform.api.ai.skill.dto.OfficeAiChatResultVO;
import com.icoolkj.platform.api.ai.skill.dto.OfficeAiSkillVO;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.security.PermissionAuthorizationService;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.oa.domain.OaAiChatMessageEntity;
import com.icoolkj.platform.oa.service.OaAiChatSessionService;
import com.icoolkj.platform.oa.vo.OaAiChatMessageVO;
import com.icoolkj.platform.oa.vo.OaAiChatSessionVO;
import com.icoolkj.platform.system.ai.constant.AiSkillConstants;
import com.icoolkj.platform.system.ai.client.OpenAiCompletionResult;
import com.icoolkj.platform.system.ai.service.AiRuntimeConfig;
import com.icoolkj.platform.system.ai.service.AiSkillCatalogService;
import com.icoolkj.platform.system.ai.client.OpenAiCompatibleChatClient;
import com.icoolkj.platform.system.ai.vo.AiSkillDefinitionVO;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Office AI skill chat: DB catalog + session history + permission-gated tool-calling.
 */
@Service
public class OfficeAiSkillChatService {

    private static final int MAX_TOOL_ROUNDS = 3;
    private static final int HISTORY_LIMIT = 12;

    private final AiSkillCatalogService catalogService;
    private final AiSkillHandlerRegistry handlerRegistry;
    private final AiSkillToolExecutor toolExecutor;
    private final OaAiChatSessionService sessionService;
    private final PermissionAuthorizationService authz;
    private final OpenAiCompatibleChatClient chatClient;
    private final AiRuntimeConfig aiRuntimeConfig;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    public OfficeAiSkillChatService(AiSkillCatalogService catalogService,
                                    AiSkillHandlerRegistry handlerRegistry,
                                    AiSkillToolExecutor toolExecutor,
                                    OaAiChatSessionService sessionService,
                                    PermissionAuthorizationService authz,
                                    OpenAiCompatibleChatClient chatClient,
                                    AiRuntimeConfig aiRuntimeConfig,
                                    MessageSource messageSource) {
        this.catalogService = catalogService;
        this.handlerRegistry = handlerRegistry;
        this.toolExecutor = toolExecutor;
        this.sessionService = sessionService;
        this.authz = authz;
        this.chatClient = chatClient;
        this.aiRuntimeConfig = aiRuntimeConfig;
        this.messageSource = messageSource;
        this.objectMapper = chatClient.getObjectMapper();
    }

    public List<OfficeAiSkillVO> listSkills(Locale locale) {
        List<AiSkillDefinitionVO> definitions = catalogService.listEnabled(AiSkillConstants.CATEGORY_OFFICE, locale);
        List<OfficeAiSkillVO> list = new ArrayList<>(definitions.size());
        for (AiSkillDefinitionVO skill : definitions) {
            OfficeAiSkillVO vo = new OfficeAiSkillVO();
            vo.setCode(skill.getSkillCode());
            vo.setName(skill.getName());
            vo.setDescription(skill.getDescription());
            vo.setRequiredPermission(skill.getRequiredPermission());
            boolean allowed = authz.hasPermission(skill.getRequiredPermission())
                    && handlerRegistry.hasHandler(skill.getSkillCode());
            vo.setAllowed(allowed);
            list.add(vo);
        }
        return list;
    }

    public OaAiChatSessionVO createSession() {
        return sessionService.createSession();
    }

    public PageResult<OaAiChatSessionVO> pageSessions(long pageNo, long pageSize) {
        return sessionService.pageMine(pageNo, pageSize);
    }

    public List<OaAiChatMessageVO> listMessages(String sessionId) {
        return sessionService.listMineMessages(sessionId);
    }

    public void deleteSession(String sessionId) {
        sessionService.deleteMine(sessionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public OfficeAiChatResultVO chat(String sessionId, String userMessage, Locale locale) {
        if (!StringUtils.hasText(userMessage)) {
            throw new BusinessException("error.ai.empty_request");
        }
        if (!StringUtils.hasText(SecurityContextHelper.getUserId())) {
            throw new BusinessException("error.unauthorized");
        }

        List<AiSkillDefinitionVO> allowed = catalogService.listEnabled(AiSkillConstants.CATEGORY_OFFICE, locale).stream()
                .filter(s -> authz.hasPermission(s.getRequiredPermission()))
                .filter(s -> handlerRegistry.hasHandler(s.getSkillCode()))
                .toList();
        if (allowed.isEmpty()) {
            throw new BusinessException("error.forbidden");
        }

        OaAiChatSessionVO session = sessionService.requireOrCreateMine(sessionId);
        String activeSessionId = session.getId();
        List<OaAiChatMessageEntity> history = sessionService.listRecentForContext(activeSessionId, HISTORY_LIMIT);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode system = messages.addObject();
        system.put("role", "system");
        system.put("content", buildSystemPrompt(locale, allowed));
        appendHistory(messages, history);

        String trimmed = userMessage.trim();
        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", trimmed);

        sessionService.appendUserMessage(activeSessionId, trimmed);

        ArrayNode tools = buildTools(allowed);
        List<String> used = new ArrayList<>();
        String emptyReply = msg("ai.office.skill.empty_reply", locale);
        String reply = emptyReply;

        for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
            OpenAiCompletionResult completion = chatClient.complete(aiRuntimeConfig, messages, tools);
            if (!completion.hasToolCalls()) {
                reply = StringUtils.hasText(completion.getContent()) ? completion.getContent() : emptyReply;
                break;
            }

            Object raw = completion.getRawAssistantMessage();
            if (raw instanceof JsonNode node) {
                messages.add(node.deepCopy());
            } else {
                ObjectNode assistant = messages.addObject();
                assistant.put("role", "assistant");
                if (StringUtils.hasText(completion.getContent())) {
                    assistant.put("content", completion.getContent());
                } else {
                    assistant.putNull("content");
                }
            }

            for (OpenAiCompletionResult.ToolCall call : completion.getToolCalls()) {
                AiSkillDefinitionVO skill = catalogService.findEnabledByToolName(call.getName(), locale).orElse(null);
                String toolContent;
                try {
                    if (skill == null
                            || !authz.hasPermission(skill.getRequiredPermission())
                            || !handlerRegistry.hasHandler(skill.getSkillCode())) {
                        toolContent = "{\"error\":\"forbidden_or_unknown_skill\"}";
                    } else {
                        toolContent = toolExecutor.executeTool(call.getName(), call.getArgumentsJson(), locale);
                        if (!used.contains(skill.getSkillCode())) {
                            used.add(skill.getSkillCode());
                        }
                    }
                } catch (BusinessException ex) {
                    toolContent = "{\"error\":\"" + safeJson(ex.getMessage()) + "\"}";
                } catch (Exception ex) {
                    toolContent = "{\"error\":\"tool_failed\"}";
                }
                ObjectNode toolMsg = messages.addObject();
                toolMsg.put("role", "tool");
                toolMsg.put("tool_call_id", call.getId());
                toolMsg.put("content", toolContent);
            }

            if (round == MAX_TOOL_ROUNDS - 1) {
                OpenAiCompletionResult finalResult = chatClient.complete(aiRuntimeConfig, messages, null);
                reply = StringUtils.hasText(finalResult.getContent()) ? finalResult.getContent() : emptyReply;
            }
        }

        sessionService.appendAssistantMessage(activeSessionId, reply, used);

        OfficeAiChatResultVO vo = new OfficeAiChatResultVO();
        vo.setSessionId(activeSessionId);
        vo.setReply(reply);
        vo.setUsedSkills(used);
        return vo;
    }

    private void appendHistory(ArrayNode messages, List<OaAiChatMessageEntity> history) {
        if (history == null || history.isEmpty()) {
            return;
        }
        for (OaAiChatMessageEntity item : history) {
            if (item == null || !StringUtils.hasText(item.getRole()) || !StringUtils.hasText(item.getContent())) {
                continue;
            }
            if (!"user".equals(item.getRole()) && !"assistant".equals(item.getRole())) {
                continue;
            }
            ObjectNode node = messages.addObject();
            node.put("role", item.getRole());
            node.put("content", item.getContent());
        }
    }

    private ArrayNode buildTools(List<AiSkillDefinitionVO> allowed) {
        ArrayNode tools = objectMapper.createArrayNode();
        for (AiSkillDefinitionVO skill : allowed) {
            ObjectNode tool = tools.addObject();
            tool.put("type", "function");
            ObjectNode fn = tool.putObject("function");
            fn.put("name", skill.getToolName());
            fn.put("description", skill.getToolDescription());
            try {
                fn.set("parameters", objectMapper.readTree(skill.getParametersSchemaJson()));
            } catch (Exception ex) {
                fn.putObject("parameters").put("type", "object");
            }
        }
        return tools;
    }

    private String buildSystemPrompt(Locale locale, List<AiSkillDefinitionVO> allowed) {
        String displayName = SecurityContextHelper.getDisplayName();
        if (!StringUtils.hasText(displayName)) {
            displayName = SecurityContextHelper.getUsername();
        }
        if (displayName == null) {
            displayName = "";
        }

        StringBuilder skillList = new StringBuilder();
        for (int i = 0; i < allowed.size(); i++) {
            if (i > 0) {
                skillList.append("; ");
            }
            AiSkillDefinitionVO s = allowed.get(i);
            skillList.append(s.getName()).append('(').append(s.getToolName()).append(')');
        }

        return msg("ai.office.skill.system_prompt", locale, displayName, skillList.toString());
    }

    private String msg(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, code, locale);
    }

    private static String safeJson(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
