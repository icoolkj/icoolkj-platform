package com.icoolkj.platform.oa.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.oa.domain.OaAiChatMessageEntity;
import com.icoolkj.platform.oa.vo.OaAiChatMessageVO;
import com.icoolkj.platform.oa.vo.OaAiChatSessionVO;

import java.util.List;

/**
 * Office AI chat sessions — tenant-scoped, owner-only.
 */
public interface OaAiChatSessionService {

    OaAiChatSessionVO createSession();

    PageResult<OaAiChatSessionVO> pageMine(long pageNo, long pageSize);

    List<OaAiChatMessageVO> listMineMessages(String sessionId);

    void deleteMine(String sessionId);

    /** Ensure session belongs to current user; create if sessionId blank. */
    OaAiChatSessionVO requireOrCreateMine(String sessionId);

    List<OaAiChatMessageEntity> listRecentForContext(String sessionId, int limit);

    void appendUserMessage(String sessionId, String content);

    void appendAssistantMessage(String sessionId, String content, List<String> usedSkills);
}
