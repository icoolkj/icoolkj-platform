package com.icoolkj.platform.oa.mapper;

import com.icoolkj.platform.oa.domain.OaAiChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OaAiChatSessionMapper {

    OaAiChatSessionEntity selectById(@Param("id") String id);

    List<OaAiChatSessionEntity> selectList(OaAiChatSessionEntity query);

    int insert(OaAiChatSessionEntity entity);

    int updateById(OaAiChatSessionEntity entity);

    int deleteById(@Param("id") String id);

    List<OaAiChatSessionEntity> selectMinePage(@Param("userId") String userId,
                                               @Param("offset") long offset,
                                               @Param("pageSize") long pageSize);

    long countMine(@Param("userId") String userId);

    OaAiChatSessionEntity selectMineById(@Param("id") String id, @Param("userId") String userId);
}
