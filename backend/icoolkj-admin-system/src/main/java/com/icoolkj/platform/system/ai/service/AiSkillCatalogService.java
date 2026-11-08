package com.icoolkj.platform.system.ai.service;

import com.icoolkj.platform.system.ai.vo.AiSkillDefinitionVO;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Platform AI skill catalog (DB-backed metadata; handlers live in api).
 */
public interface AiSkillCatalogService {

    /**
     * Enabled skills for a category, names/descriptions resolved for locale.
     */
    List<AiSkillDefinitionVO> listEnabled(String category, Locale locale);

    Optional<AiSkillDefinitionVO> findEnabledByToolName(String toolName, Locale locale);

    Optional<AiSkillDefinitionVO> findEnabledBySkillCode(String skillCode, Locale locale);
}
