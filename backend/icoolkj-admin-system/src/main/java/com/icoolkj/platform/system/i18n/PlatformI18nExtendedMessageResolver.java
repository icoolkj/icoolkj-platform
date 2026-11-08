package com.icoolkj.platform.system.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.i18n.ExtendedMessageResolver;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves platform fixed keys from Git classpath JSON
 * ({@code error.*} / {@code entity.name.*} / {@code validation.*} / {@code notify.*} /
 * {@code oa.instance.*} / {@code tenant.bootstrap.*}). DB rows in {@code sys_i18n_message} still win because
 * {@code I18nQueryServiceImpl} checks the database first.
 */
@Component
public class PlatformI18nExtendedMessageResolver implements ExtendedMessageResolver {

    private static final TypeReference<Map<String, String>> FLAT_MAP = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Map<String, String>> cache = new ConcurrentHashMap<>();

    public PlatformI18nExtendedMessageResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String resolve(String code, Object[] args, Locale locale) {
        if (!StringUtils.hasText(code) || !isPlatformKey(code)) {
            return null;
        }
        String normalizedLocale = LocaleResolver.normalize(locale != null ? locale.toLanguageTag() : null);
        String template = getMessageMap(normalizedLocale).get(code);
        if (!StringUtils.hasText(template) && !LocaleResolver.LOCALE_ZH_CN.equals(normalizedLocale)) {
            template = getMessageMap(LocaleResolver.LOCALE_ZH_CN).get(code);
        }
        return StringUtils.hasText(template) ? template : null;
    }

    static boolean isPlatformKey(String code) {
        return code.startsWith("error.")
                || code.startsWith("entity.name.")
                || code.startsWith("validation.")
                || code.startsWith("notify.")
                || code.startsWith("oa.instance.")
                || code.startsWith("tenant.bootstrap.");
    }

    private Map<String, String> getMessageMap(String locale) {
        return cache.computeIfAbsent(locale, this::loadLocale);
    }

    private Map<String, String> loadLocale(String locale) {
        return PlatformClasspathBaselineLoader.loadI18nJson(locale)
                .map(json -> {
                    try {
                        Map<String, String> map = objectMapper.readValue(json, FLAT_MAP);
                        return map != null ? Map.copyOf(map) : Collections.<String, String>emptyMap();
                    } catch (Exception ignored) {
                        return Collections.<String, String>emptyMap();
                    }
                })
                .orElse(Collections.emptyMap());
    }
}
