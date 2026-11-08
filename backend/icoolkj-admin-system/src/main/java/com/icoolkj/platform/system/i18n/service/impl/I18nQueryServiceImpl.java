package com.icoolkj.platform.system.i18n.service.impl;

import com.icoolkj.platform.common.constant.I18nScopeConstants;
import com.icoolkj.platform.common.i18n.ExtendedMessageResolver;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.system.i18n.convert.I18nConvert;
import com.icoolkj.platform.system.i18n.domain.I18nLocaleEntity;
import com.icoolkj.platform.system.i18n.domain.I18nMessageEntity;
import com.icoolkj.platform.system.i18n.mapper.I18nLocaleMapper;
import com.icoolkj.platform.system.i18n.mapper.I18nMessageMapper;
import com.icoolkj.platform.system.i18n.service.I18nCacheService;
import com.icoolkj.platform.system.i18n.service.I18nQueryService;
import com.icoolkj.platform.system.i18n.vo.I18nBootstrapVO;
import com.icoolkj.platform.system.i18n.vo.I18nLocaleVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class I18nQueryServiceImpl implements I18nQueryService {

    private final I18nLocaleMapper i18nLocaleMapper;
    private final I18nMessageMapper i18nMessageMapper;
    private final I18nCacheService i18nCacheService;
    private final List<ExtendedMessageResolver> extendedMessageResolvers;

    public I18nQueryServiceImpl(I18nLocaleMapper i18nLocaleMapper,
                                I18nMessageMapper i18nMessageMapper,
                                I18nCacheService i18nCacheService,
                                List<ExtendedMessageResolver> extendedMessageResolvers) {
        this.i18nLocaleMapper = i18nLocaleMapper;
        this.i18nMessageMapper = i18nMessageMapper;
        this.i18nCacheService = i18nCacheService;
        this.extendedMessageResolvers = extendedMessageResolvers != null ? extendedMessageResolvers : List.of();
    }

    @Override
    public I18nBootstrapVO getBootstrap() {
        List<I18nLocaleEntity> enabled = i18nLocaleMapper.selectEnabled();
        I18nLocaleEntity defaultLocale = i18nLocaleMapper.selectDefault();
        String defaultCode = defaultLocale != null
                ? defaultLocale.getLocaleCode()
                : LocaleResolver.LOCALE_ZH_CN;

        I18nBootstrapVO vo = new I18nBootstrapVO();
        vo.setDefaultLocale(defaultCode);
        vo.setSupportedLocales(enabled.stream().map(I18nLocaleEntity::getLocaleCode).toList());
        vo.setLocales(enabled.stream().map(I18nConvert::toLocaleVo).toList());
        return vo;
    }

    @Override
    public Map<String, String> getMessageMap(String locale, String scope) {
        String normalizedLocale = LocaleResolver.normalize(locale);
        List<String> scopes = resolveScopes(scope);

        Map<String, String> cached = i18nCacheService.getMessageMap(normalizedLocale, scopes);
        if (cached != null) {
            return cached;
        }

        List<I18nMessageEntity> rows = i18nMessageMapper.selectByLocaleAndScopes(normalizedLocale, scopes);
        Map<String, String> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(rows)) {
            for (I18nMessageEntity row : rows) {
                map.put(row.getMessageKey(), row.getMessageValue());
            }
        }
        i18nCacheService.putMessageMap(normalizedLocale, scopes, map);
        return map;
    }

    @Override
    public String resolve(String code, Object[] args, Locale locale) {
        if (!StringUtils.hasText(code)) {
            return "";
        }
        String normalizedLocale = LocaleResolver.normalize(locale != null ? locale.toLanguageTag() : null);
        Map<String, String> messages = getMessageMap(normalizedLocale, I18nScopeConstants.BACKEND);
        String template = messages.get(code);
        if (!StringUtils.hasText(template) && !LocaleResolver.LOCALE_ZH_CN.equals(normalizedLocale)) {
            Map<String, String> fallback = getMessageMap(LocaleResolver.LOCALE_ZH_CN, I18nScopeConstants.BACKEND);
            template = fallback.get(code);
        }
        if (!StringUtils.hasText(template)) {
            template = resolveExtended(code, args, locale);
        }
        if (!StringUtils.hasText(template)) {
            return code;
        }
        if (args == null || args.length == 0) {
            return template;
        }
        return MessageFormat.format(template, args);
    }

    @Override
    public List<String> getEnabledLocaleCodes() {
        List<String> cached = i18nCacheService.getEnabledLocaleCodes();
        if (cached != null) {
            return cached;
        }
        List<String> codes = i18nLocaleMapper.selectEnabled().stream()
                .map(I18nLocaleEntity::getLocaleCode)
                .collect(Collectors.toList());
        i18nCacheService.putEnabledLocaleCodes(codes);
        return codes;
    }

    private List<String> resolveScopes(String scope) {
        if (I18nScopeConstants.FRONTEND.equals(scope)) {
            return List.of(I18nScopeConstants.FRONTEND, I18nScopeConstants.ALL);
        }
        if (I18nScopeConstants.BACKEND.equals(scope)) {
            return List.of(I18nScopeConstants.BACKEND, I18nScopeConstants.ALL);
        }
        return List.of(I18nScopeConstants.FRONTEND, I18nScopeConstants.BACKEND, I18nScopeConstants.ALL);
    }

    private String resolveExtended(String code, Object[] args, Locale locale) {
        for (ExtendedMessageResolver resolver : extendedMessageResolvers) {
            String resolved = resolver.resolve(code, args, locale);
            if (StringUtils.hasText(resolved)) {
                return resolved;
            }
        }
        return null;
    }
}
