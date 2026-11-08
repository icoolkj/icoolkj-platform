package com.icoolkj.platform.oa.mapper;

import com.icoolkj.platform.oa.domain.OaAiChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OaAiChatMessageMapper {

    OaAiChatMessageEntity selectById(@Param("id") String id);

    List<OaAiChatMessageEntity> selectList(OaAiChatMessageEntity query);

    int insert(OaAiChatMessageEntity entity);

    int updateById(OaAiChatMessageEntity entity);

    int deleteById(@Param("id") String id);

    List<OaAiChatMessageEntity> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * Latest N messages for a session (ascending by created_at after fetch order reverse).
     */
    List<OaAiChatMessageEntity> selectLatestBySessionId(@Param("sessionId") String sessionId,
                                                        @Param("limit") int limit);

    int softDeleteBySessionId(@Param("sessionId") String sessionId);
}
