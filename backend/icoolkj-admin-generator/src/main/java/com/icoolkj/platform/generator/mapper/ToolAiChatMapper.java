package com.icoolkj.platform.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.icoolkj.platform.generator.domain.ToolAiChatMessage;
import com.icoolkj.platform.generator.domain.ToolAiChatMessageContent;
import com.icoolkj.platform.generator.domain.ToolAiChatSession;
import com.icoolkj.platform.generator.dto.ToolAiChatSessionPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface ToolAiChatMapper {

    int insertSession(ToolAiChatSession session);

    int updateSession(ToolAiChatSession session);

    ToolAiChatSession selectSessionById(@Param("id") String id);

    long countSessionByQuery(ToolAiChatSessionPageQuery query);

    List<ToolAiChatSession> selectSessionPage(ToolAiChatSessionPageQuery query);

    int insertMessage(ToolAiChatMessage message);

    int insertMessageContent(ToolAiChatMessageContent content);

    List<ToolAiChatMessage> selectMessagesBySessionId(@Param("sessionId") String sessionId);

    ToolAiChatMessageContent selectMessageContentById(@Param("messageId") String messageId);

    int archiveSession(@Param("id") String id);
}
