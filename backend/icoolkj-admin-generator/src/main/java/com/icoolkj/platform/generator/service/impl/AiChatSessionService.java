package com.icoolkj.platform.generator.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.generator.domain.ToolAiChatMessage;
import com.icoolkj.platform.generator.domain.ToolAiChatMessageContent;
import com.icoolkj.platform.generator.domain.ToolAiChatSession;
import com.icoolkj.platform.generator.dto.ToolAiChatSendRequest;
import com.icoolkj.platform.generator.dto.ToolAiChatSessionPageQuery;
import com.icoolkj.platform.generator.mapper.ToolAiChatMapper;
import com.icoolkj.platform.generator.vo.AiChatResultVO;
import com.icoolkj.platform.generator.vo.ToolAiChatMessageVO;
import com.icoolkj.platform.generator.vo.ToolAiChatSendResultVO;
import com.icoolkj.platform.generator.vo.ToolAiChatSessionVO;
import com.icoolkj.platform.system.ai.dto.AiChatMessageItem;
import com.icoolkj.platform.system.ai.service.AiRuntimeConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AiChatSessionService {

    private static final int PREVIEW_LEN = 500;
    private static final int HISTORY_LIMIT = 20;
    private static final String SYSTEM_PROMPT = """
            你是 icoolkj 低代码平台开发助手，熟悉 Java/Spring Boot、Vue3、MySQL、代码生成与页面规格。
            请用简洁、可落地的中文回答；涉及代码时使用 Markdown 代码块。
            """;

    private final AiRuntimeConfig aiRuntimeConfig;
    private final AiChatFacade aiChatFacade;
    private final ToolAiChatMapper toolAiChatMapper;

    public AiChatSessionService(AiRuntimeConfig aiRuntimeConfig,
                                AiChatFacade aiChatFacade,
                                ToolAiChatMapper toolAiChatMapper) {
        this.aiRuntimeConfig = aiRuntimeConfig;
        this.aiChatFacade = aiChatFacade;
        this.toolAiChatMapper = toolAiChatMapper;
    }

    public PageResult<ToolAiChatSessionVO> pageSessions(ToolAiChatSessionPageQuery query) {
        TenantQueryScope.bindQueryTenantId(query::setTenantId);
        if (!Boolean.FALSE.equals(query.getMineOnly())) {
            query.setOperatorId(currentUserId());
        }
        long total = toolAiChatMapper.countSessionByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, toLong(query.getPageNo()), toLong(query.getPageSize()));
        }
        List<ToolAiChatSessionVO> rows = toolAiChatMapper.selectSessionPage(query).stream().map(this::toSessionVo).toList();
        return PageResult.of(rows, total, toLong(query.getPageNo()), toLong(query.getPageSize()));
    }

    @Transactional
    public ToolAiChatSessionVO createSession() {
        assertAiReady();
        LocalDateTime now = LocalDateTime.now();
        ToolAiChatSession session = new ToolAiChatSession();
        session.setId(IdGeneratorUtils.nextIdStr());
        session.setTenantId(currentTenantId());
        session.setTitle(null);
        session.setStatus("ACTIVE");
        session.setOperatorId(currentUserId());
        session.setUserId(currentUserId());
        session.setOperatorName(SecurityContextHelper.getUsername());
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        toolAiChatMapper.insertSession(session);
        return toSessionVo(session);
    }

    public ToolAiChatSessionVO getSession(String sessionId, boolean mineOnly) {
        ToolAiChatSession session = requireSession(sessionId, mineOnly);
        ToolAiChatSessionVO vo = toSessionVo(session);
        vo.setMessages(loadMessages(sessionId));
        return vo;
    }

    public ToolAiChatSendResultVO sendMessage(String sessionId, ToolAiChatSendRequest request) {
        assertAiReady();
        ToolAiChatSession session = requireOwnedSession(sessionId);
        String userText = request.getContent().trim();

        ToolAiChatMessageVO userVo = saveMessage(sessionId, "user", userText, null);

        List<AiChatMessageItem> history = buildHistory(sessionId);
        AiChatResultVO aiResult = aiChatFacade.chatConversation(
                aiRuntimeConfig, SYSTEM_PROMPT, history, userText, sessionId);
        ToolAiChatMessageVO assistantVo = saveMessage(sessionId, "assistant", aiResult.getContent(), aiResult.getInvokeId());

        LocalDateTime now = LocalDateTime.now();
        session.setUpdatedAt(now);
        if (StringUtils.isBlank(session.getTitle())) {
            session.setTitle(buildTitle(userText));
        }
        toolAiChatMapper.updateSession(session);

        ToolAiChatSendResultVO result = new ToolAiChatSendResultVO();
        result.setUserMessage(userVo);
        result.setAssistantMessage(assistantVo);
        return result;
    }

    @Transactional
    public void archiveSession(String sessionId) {
        requireOwnedSession(sessionId);
        toolAiChatMapper.archiveSession(sessionId);
    }

    private List<AiChatMessageItem> buildHistory(String sessionId) {
        List<ToolAiChatMessage> rows = toolAiChatMapper.selectMessagesBySessionId(sessionId);
        if (rows.size() <= 1) {
            return List.of();
        }
        int start = Math.max(0, rows.size() - 1 - HISTORY_LIMIT);
        List<AiChatMessageItem> history = new ArrayList<>();
        for (int i = start; i < rows.size() - 1; i++) {
            ToolAiChatMessage row = rows.get(i);
            if ("system".equals(row.getRole())) {
                continue;
            }
            String content = loadContent(row.getId());
            if (StringUtils.isNotBlank(content)) {
                history.add(new AiChatMessageItem(row.getRole(), content));
            }
        }
        return history;
    }

    private ToolAiChatMessageVO saveMessage(String sessionId, String role, String content, String invokeId) {
        String messageId = IdGeneratorUtils.nextIdStr();
        LocalDateTime now = LocalDateTime.now();
        ToolAiChatMessage message = new ToolAiChatMessage();
        message.setId(messageId);
        message.setSessionId(sessionId);
        message.setInvokeId(invokeId);
        message.setRole(role);
        message.setContentPreview(truncate(content, PREVIEW_LEN));
        message.setCreatedAt(now);
        toolAiChatMapper.insertMessage(message);

        ToolAiChatMessageContent body = new ToolAiChatMessageContent();
        body.setMessageId(messageId);
        body.setContent(content);
        toolAiChatMapper.insertMessageContent(body);

        ToolAiChatMessageVO vo = new ToolAiChatMessageVO();
        vo.setId(messageId);
        vo.setSessionId(sessionId);
        vo.setInvokeId(invokeId);
        vo.setRole(role);
        vo.setContent(content);
        vo.setCreatedAt(now);
        return vo;
    }

    private List<ToolAiChatMessageVO> loadMessages(String sessionId) {
        return toolAiChatMapper.selectMessagesBySessionId(sessionId).stream().map(row -> {
            ToolAiChatMessageVO vo = new ToolAiChatMessageVO();
            vo.setId(row.getId());
            vo.setSessionId(row.getSessionId());
            vo.setInvokeId(row.getInvokeId());
            vo.setRole(row.getRole());
            vo.setContent(loadContent(row.getId()));
            vo.setCreatedAt(row.getCreatedAt());
            return vo;
        }).toList();
    }

    private String loadContent(String messageId) {
        ToolAiChatMessageContent content = toolAiChatMapper.selectMessageContentById(messageId);
        return content != null ? content.getContent() : null;
    }

    private ToolAiChatSession requireOwnedSession(String sessionId) {
        ToolAiChatSession session = requireSession(sessionId, true);
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new BusinessException("error.entity.not_found", "entity.name.ai_chat_session");
        }
        return session;
    }

    private ToolAiChatSession requireSession(String sessionId, boolean mineOnly) {
        ToolAiChatSession session = toolAiChatMapper.selectSessionById(sessionId);
        if (session == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.ai_chat_session");
        }
        TenantQueryScope.assertReadable(session.getTenantId());
        if (mineOnly) {
            String userId = currentUserId();
            if (StringUtils.isNotBlank(userId) && !userId.equals(session.getOperatorId())) {
                throw new BusinessException("error.forbidden");
            }
        }
        return session;
    }

    private void assertAiReady() {
        if (!aiRuntimeConfig.isEnabled() || StringUtils.isBlank(aiRuntimeConfig.getApiKey())) {
            throw new BusinessException("error.ai.not_enabled");
        }
    }

    private ToolAiChatSessionVO toSessionVo(ToolAiChatSession session) {
        ToolAiChatSessionVO vo = new ToolAiChatSessionVO();
        vo.setId(session.getId());
        vo.setTitle(session.getTitle());
        vo.setStatus(session.getStatus());
        vo.setOperatorName(session.getOperatorName());
        vo.setCreatedAt(session.getCreatedAt());
        vo.setUpdatedAt(session.getUpdatedAt());
        return vo;
    }

    private static String buildTitle(String userText) {
        String normalized = userText.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 40) {
            return normalized;
        }
        return normalized.substring(0, 40) + "…";
    }

    private static String currentTenantId() {
        String tenantId = SecurityContextHelper.getTenantId();
        return StringUtils.isNotBlank(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID;
    }

    private static String currentUserId() {
        return SecurityContextHelper.getUserId();
    }

    private static String truncate(String text, int max) {
        if (text == null || max <= 0) {
            return text;
        }
        if (text.length() <= max) {
            return text;
        }
        if (max == 1) {
            return "…";
        }
        return text.substring(0, max - 1) + "…";
    }

    private static Long toLong(Integer value) {
        return value == null ? 1L : value.longValue();
    }
}
