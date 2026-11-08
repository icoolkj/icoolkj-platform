package com.icoolkj.platform.system.ai.service.impl;

import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.system.ai.domain.AiSkillEntity;
import com.icoolkj.platform.system.ai.mapper.AiSkillMapper;
import com.icoolkj.platform.system.ai.service.AiSkillCatalogService;
import com.icoolkj.platform.system.menu.support.MenuNameI18nHelper;
import com.icoolkj.platform.system.ai.vo.AiSkillDefinitionVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AiSkillCatalogServiceImpl implements AiSkillCatalogService {

    private final AiSkillMapper aiSkillMapper;

    public AiSkillCatalogServiceImpl(AiSkillMapper aiSkillMapper) {
        this.aiSkillMapper = aiSkillMapper;
    }

    @Override
    public List<AiSkillDefinitionVO> listEnabled(String category, Locale locale) {
        List<AiSkillEntity> rows = aiSkillMapper.selectEnabled(category);
        List<AiSkillDefinitionVO> result = new ArrayList<>(rows.size());
        for (AiSkillEntity row : rows) {
            result.add(toDefinition(row, locale));
        }
        return result;
    }

    @Override
    public Optional<AiSkillDefinitionVO> findEnabledByToolName(String toolName, Locale locale) {
        if (!StringUtils.hasText(toolName)) {
            return Optional.empty();
        }
        AiSkillEntity entity = aiSkillMapper.selectByToolName(toolName.trim());
        if (entity == null || !isEnabled(entity)) {
            return Optional.empty();
        }
        return Optional.of(toDefinition(entity, locale));
    }

    @Override
    public Optional<AiSkillDefinitionVO> findEnabledBySkillCode(String skillCode, Locale locale) {
        if (!StringUtils.hasText(skillCode)) {
            return Optional.empty();
        }
        AiSkillEntity entity = aiSkillMapper.selectBySkillCode(skillCode.trim());
        if (entity == null || !isEnabled(entity)) {
            return Optional.empty();
        }
        return Optional.of(toDefinition(entity, locale));
    }

    private static boolean isEnabled(AiSkillEntity entity) {
        return entity.getStatus() == null || "0".equals(entity.getStatus());
    }

    private static AiSkillDefinitionVO toDefinition(AiSkillEntity entity, Locale locale) {
        String localeTag = locale == null
                ? LocaleResolver.LOCALE_ZH_CN
                : LocaleResolver.normalize(locale.toLanguageTag());
        AiSkillDefinitionVO vo = new AiSkillDefinitionVO();
        vo.setId(entity.getId());
        vo.setSkillCode(entity.getSkillCode());
        vo.setToolName(entity.getToolName());
        vo.setCategory(entity.getCategory());
        vo.setName(MenuNameI18nHelper.resolve(localeTag, entity.getNameI18n(), entity.getSkillName()));
        vo.setDescription(MenuNameI18nHelper.resolve(localeTag, entity.getDescriptionI18n(), entity.getDescription()));
        vo.setRequiredPermission(entity.getRequiredPermission());
        vo.setToolDescription(entity.getToolDescription());
        vo.setParametersSchemaJson(entity.getParametersSchemaJson());
        vo.setSortNo(entity.getSortNo());
        return vo;
    }
}
