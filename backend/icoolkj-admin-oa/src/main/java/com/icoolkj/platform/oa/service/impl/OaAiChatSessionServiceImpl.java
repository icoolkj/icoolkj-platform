package com.icoolkj.platform.oa.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.oa.domain.OaAiChatMessageEntity;
import com.icoolkj.platform.oa.domain.OaAiChatSessionEntity;
import com.icoolkj.platform.oa.mapper.OaAiChatMessageMapper;
import com.icoolkj.platform.oa.mapper.OaAiChatSessionMapper;
import com.icoolkj.platform.oa.service.OaAiChatSessionService;
import com.icoolkj.platform.oa.vo.OaAiChatMessageVO;
import com.icoolkj.platform.oa.vo.OaAiChatSessionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OaAiChatSessionServiceImpl implements OaAiChatSessionService {

    private static final int TITLE_MAX = 40;
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final OaAiChatSessionMapper sessionMapper;
    private final OaAiChatMessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    public OaAiChatSessionServiceImpl(OaAiChatSessionMapper sessionMapper,
                                      OaAiChatMessageMapper messageMapper,
                                      ObjectMapper objectMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaAiChatSessionVO createSession() {
        OaAiChatSessionEntity entity = newSessionEntity();
        sessionMapper.insert(entity);
        return toSessionVo(entity);
    }

    @Override
    public PageResult<OaAiChatSessionVO> pageMine(long pageNo, long pageSize) {
        String userId = requireUserId();
        long safePageNo = pageNo < 1 ? 1 : pageNo;
        long safePageSize = pageSize < 1 ? 20 : Math.min(pageSize, 100);
        long total = sessionMapper.countMine(userId);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, safePageNo, safePageSize);
        }
        long offset = (safePageNo - 1) * safePageSize;
        List<OaAiChatSessionVO> rows = sessionMapper.selectMinePage(userId, offset, safePageSize).stream()
                .map(this::toSessionVo)
                .toList();
        return PageResult.of(rows, total, safePageNo, safePageSize);
    }

    @Override
    public List<OaAiChatMessageVO> listMineMessages(String sessionId) {
        requireMineSession(sessionId);
        return messageMapper.selectBySessionId(sessionId).stream().map(this::toMessageVo).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMine(String sessionId) {
        OaAiChatSessionEntity session = requireMineSession(sessionId);
        messageMapper.softDeleteBySessionId(sessionId);
        AuditFieldHelper.markDeleted(session);
        sessionMapper.updateById(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaAiChatSessionVO requireOrCreateMine(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return createSession();
        }
        return toSessionVo(requireMineSession(sessionId.trim()));
    }

    @Override
    public List<OaAiChatMessageEntity> listRecentForContext(String sessionId, int limit) {
        requireMineSession(sessionId);
        int safeLimit = Math.max(1, Math.min(limit, 40));
        return messageMapper.selectLatestBySessionId(sessionId, safeLimit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appendUserMessage(String sessionId, String content) {
        OaAiChatSessionEntity session = requireMineSession(sessionId);
        insertMessage(sessionId, "user", content, null);
        bumpSession(session, content, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appendAssistantMessage(String sessionId, String content, List<String> usedSkills) {
        OaAiChatSessionEntity session = requireMineSession(sessionId);
        insertMessage(sessionId, "assistant", content, usedSkills);
        bumpSession(session, null, false);
    }

    private void insertMessage(String sessionId, String role, String content, List<String> usedSkills) {
        OaAiChatMessageEntity msg = new OaAiChatMessageEntity();
        msg.setId(IdGeneratorUtils.nextIdStr());
        msg.setTenantId(requireTenantId());
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content == null ? "" : content);
        msg.setUsedSkillsJson(toSkillsJson(usedSkills));
        AuditFieldHelper.fillInsert(msg);
        messageMapper.insert(msg);
    }

    private void bumpSession(OaAiChatSessionEntity session, String firstUserContent, boolean maybeTitle) {
        LocalDateTime now = LocalDateTime.now();
        session.setLastMessageAt(now);
        int count = session.getMessageCount() == null ? 0 : session.getMessageCount();
        session.setMessageCount(count + 1);
        if (maybeTitle && !StringUtils.hasText(session.getTitle()) && StringUtils.hasText(firstUserContent)) {
            session.setTitle(truncateTitle(firstUserContent.trim()));
        }
        AuditFieldHelper.fillUpdate(session);
        sessionMapper.updateById(session);
    }

    private OaAiChatSessionEntity newSessionEntity() {
        OaAiChatSessionEntity entity = new OaAiChatSessionEntity();
        entity.setId(IdGeneratorUtils.nextIdStr());
        entity.setTenantId(requireTenantId());
        entity.setUserId(requireUserId());
        entity.setStatus(CommonConstants.STATUS_NORMAL);
        entity.setMessageCount(0);
        entity.setLastMessageAt(LocalDateTime.now());
        AuditFieldHelper.fillInsert(entity);
        return entity;
    }

    private OaAiChatSessionEntity requireMineSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new BusinessException("error.ai.session_not_found");
        }
        OaAiChatSessionEntity session = sessionMapper.selectMineById(sessionId.trim(), requireUserId());
        if (session == null) {
            throw new BusinessException("error.ai.session_not_found");
        }
        return session;
    }

    private OaAiChatSessionVO toSessionVo(OaAiChatSessionEntity entity) {
        OaAiChatSessionVO vo = new OaAiChatSessionVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setStatus(entity.getStatus());
        vo.setLastMessageAt(entity.getLastMessageAt());
        vo.setMessageCount(entity.getMessageCount());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    private OaAiChatMessageVO toMessageVo(OaAiChatMessageEntity entity) {
        OaAiChatMessageVO vo = new OaAiChatMessageVO();
        vo.setId(entity.getId());
        vo.setSessionId(entity.getSessionId());
        vo.setRole(entity.getRole());
        vo.setContent(entity.getContent());
        vo.setUsedSkills(parseSkills(entity.getUsedSkillsJson()));
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    private String toSkillsJson(List<String> usedSkills) {
        if (usedSkills == null || usedSkills.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(usedSkills);
        } catch (Exception ex) {
            return null;
        }
    }

    private List<String> parseSkills(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            List<String> list = objectMapper.readValue(json, STRING_LIST);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private static String truncateTitle(String text) {
        String oneLine = text.replace('\n', ' ').trim();
        if (oneLine.length() <= TITLE_MAX) {
            return oneLine;
        }
        return oneLine.substring(0, TITLE_MAX) + "…";
    }

    private static String requireUserId() {
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("error.unauthorized");
        }
        return userId;
    }

    private static String requireTenantId() {
        String tenantId = TenantContextHolder.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException("error.unauthorized");
        }
        return tenantId;
    }
}
