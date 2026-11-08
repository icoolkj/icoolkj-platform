package com.icoolkj.platform.system.ai.mapper;

import com.icoolkj.platform.system.ai.domain.AiSkillEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiSkillMapper {

    AiSkillEntity selectById(@Param("id") String id);

    List<AiSkillEntity> selectList(AiSkillEntity query);

    int insert(AiSkillEntity entity);

    int updateById(AiSkillEntity entity);

    int deleteById(@Param("id") String id);

    AiSkillEntity selectBySkillCode(@Param("skillCode") String skillCode);

    AiSkillEntity selectByToolName(@Param("toolName") String toolName);

    /**
     * Enabled skills, optionally filtered by category, ordered by sort_no.
     */
    List<AiSkillEntity> selectEnabled(@Param("category") String category);
}
