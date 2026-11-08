package com.icoolkj.platform.generator.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.generator.domain.GenI18nMessage;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.mapper.GenI18nMessageMapper;
import com.icoolkj.platform.generator.service.GenI18nMessageService;
import com.icoolkj.platform.generator.util.GenClasspathBaselineLoader;
import com.icoolkj.platform.generator.util.GenI18nBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GenI18nMessageServiceImpl implements GenI18nMessageService {

    private static final List<String> DEFAULT_LOCALES = List.of(
            LocaleResolver.LOCALE_ZH_CN,
            LocaleResolver.LOCALE_EN_US
    );

    private static final String CLASSPATH_GEN_I18N_PATTERN = "classpath*:i18n/gen/*/%s.json";

    private final GenI18nMessageMapper genI18nMessageMapper;
    private final ResourcePatternResolver resourcePatternResolver;
    private final Map<String, Map<String, String>> validationCache = new ConcurrentHashMap<>();

    public GenI18nMessageServiceImpl(GenI18nMessageMapper genI18nMessageMapper) {
        this.genI18nMessageMapper = genI18nMessageMapper;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
    }

    @Override
    public List<GenI18nMessage> listByTableId(String tableId) {
        return genI18nMessageMapper.selectByTableId(tableId);
    }

    @Override
    public GenI18nMessage getByTableIdAndLocale(String tableId, String locale) {
        return genI18nMessageMapper.selectByTableIdAndLocale(tableId, normalize(locale));
    }

    @Override
    public GenI18nMessage getRuntimeMessages(String tableId, String locale) {
        return getByTableIdAndLocale(tableId, locale);
    }

    @Override
    public GenI18nMessage getRuntimeMessagesByBusiness(String businessName, String apiModule, String locale) {
        if (!StringUtils.hasText(businessName) || !StringUtils.hasText(apiModule)) {
            return null;
        }
        return genI18nMessageMapper.selectByBusinessAndLocale(
                businessName.trim(), apiModule.trim(), normalize(locale));
    }

    @Override
    @Transactional
    public void save(GenI18nMessage row) {
        row.setLocale(normalize(row.getLocale()));
        if (StringUtils.hasText(row.getId())) {
            genI18nMessageMapper.update(row);
        } else {
            row.setId(IdGeneratorUtils.nextIdStr());
            genI18nMessageMapper.insert(row);
        }
        evictCache(row.getLocale());
    }

    @Override
    @Transactional
    public void syncFromTable(GenTable table) {
        if (table == null || !StringUtils.hasText(table.getId())) {
            return;
        }
        for (String locale : DEFAULT_LOCALES) {
            String fresh = GenI18nBuilder.buildJson(table, table.getColumns(), locale);
            GenI18nMessage existing = genI18nMessageMapper.selectByTableIdAndLocale(table.getId(), locale);
            if (existing == null) {
                GenI18nMessage row = new GenI18nMessage();
                row.setId(IdGeneratorUtils.nextIdStr());
                row.setTableId(table.getId());
                row.setLocale(locale);
                row.setMessagesJson(fresh);
                genI18nMessageMapper.insert(row);
            } else {
                existing.setMessagesJson(GenI18nBuilder.mergeJson(existing.getMessagesJson(), fresh, locale));
                genI18nMessageMapper.update(existing);
            }
            evictCache(locale);
        }
    }

    @Override
    @Transactional
    public void deleteByTableId(String tableId) {
        genI18nMessageMapper.deleteByTableId(tableId);
        validationCache.clear();
    }

    @Override
    public Map<String, String> getValidationMessageMap(String locale) {
        String normalized = normalize(locale);
        return validationCache.computeIfAbsent(normalized, this::loadValidationMap);
    }

    @Override
    public String getGitBaselineJson(String businessName, String locale) {
        String normalized = normalize(locale);
        return GenClasspathBaselineLoader.loadI18nJson(businessName, normalized)
                .orElseThrow(() -> new BusinessException("error.gen.baseline_not_found", businessName, normalized + ".json"));
    }

    @Override
    @Transactional
    public void restoreGitBaseline(String tableId, String businessName, String locale) {
        String normalized = normalize(locale);
        String baseline = getGitBaselineJson(businessName, normalized);
        GenI18nMessage existing = genI18nMessageMapper.selectByTableIdAndLocale(tableId, normalized);
        if (existing == null) {
            GenI18nMessage row = new GenI18nMessage();
            row.setId(IdGeneratorUtils.nextIdStr());
            row.setTableId(tableId);
            row.setLocale(normalized);
            row.setMessagesJson(baseline);
            genI18nMessageMapper.insert(row);
        } else {
            existing.setMessagesJson(baseline);
            genI18nMessageMapper.update(existing);
        }
        evictCache(normalized);
    }

    /** Git classpath JSON only — gen_i18n_message DB is not merged at runtime (P0). */
    private Map<String, String> loadValidationMap(String locale) {
        Map<String, String> map = new HashMap<>(loadClasspathValidationMap(locale));
        if (!LocaleResolver.LOCALE_ZH_CN.equals(locale)) {
            loadClasspathValidationMap(LocaleResolver.LOCALE_ZH_CN).forEach(map::putIfAbsent);
        }
        return map;
    }

    /** Git baseline: {@code classpath:i18n/gen/{businessName}/{locale}.json} → gen.{businessName}.{field}.{rule}. */
    private Map<String, String> loadClasspathValidationMap(String locale) {
        Map<String, String> map = new HashMap<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources(
                    String.format(CLASSPATH_GEN_I18N_PATTERN, locale));
            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    continue;
                }
                String uri = resource.getURI().toString().replace('\\', '/');
                // Codegen phrase templates only — not a runtime business pack
                if (uri.contains("/i18n/gen/_codegen/")) {
                    continue;
                }
                String json = resource.getContentAsString(StandardCharsets.UTF_8);
                map.putAll(GenI18nBuilder.flattenValidationKeys(json));
            }
        } catch (IOException ignored) {
            // no classpath gen i18n resources
        }
        return map;
    }

    private void evictCache(String locale) {
        validationCache.remove(normalize(locale));
        validationCache.remove(LocaleResolver.LOCALE_ZH_CN);
    }

    private static String normalize(String locale) {
        return LocaleResolver.normalize(locale);
    }
}
