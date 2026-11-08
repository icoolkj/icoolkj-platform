package com.icoolkj.platform.system.i18n.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.CacheKeyConstants;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.icoolkj.platform.system.i18n.service.I18nCacheService;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class I18nCacheServiceImpl implements I18nCacheService {

    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
    };

    private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate stringRedisTemplate;
    private final CacheTtlProperties cacheTtlProperties;
    private final ObjectMapper objectMapper;

    public I18nCacheServiceImpl(StringRedisTemplate stringRedisTemplate,
                                CacheTtlProperties cacheTtlProperties,
                                ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheTtlProperties = cacheTtlProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, String> getMessageMap(String locale, List<String> scopes) {
        String raw = stringRedisTemplate.opsForValue().get(buildMessageKey(locale, scopes));
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, MAP_TYPE);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void putMessageMap(String locale, List<String> scopes, Map<String, String> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(
                    buildMessageKey(locale, scopes),
                    objectMapper.writeValueAsString(messages),
                    messageTtl()
            );
        } catch (Exception ignored) {
            // cache is best-effort
        }
    }

    @Override
    public void evictMessages(String locale) {
        String pattern = cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.I18N_MESSAGES + locale + ":*";
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Override
    public void refreshAllMessages() {
        String pattern = cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.I18N_MESSAGES + "*";
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Override
    public List<String> getEnabledLocaleCodes() {
        String raw = stringRedisTemplate.opsForValue().get(buildLocalesKey());
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, LIST_TYPE);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void putEnabledLocaleCodes(List<String> codes) {
        if (codes == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(
                    buildLocalesKey(),
                    objectMapper.writeValueAsString(codes),
                    messageTtl()
            );
        } catch (Exception ignored) {
            // cache is best-effort
        }
    }

    @Override
    public void evictEnabledLocales() {
        stringRedisTemplate.delete(buildLocalesKey());
    }

    private String buildMessageKey(String locale, List<String> scopes) {
        List<String> normalized = scopes == null ? Collections.emptyList() : scopes.stream().sorted().toList();
        return cacheTtlProperties.getKeyPrefix() + ":"
                + CacheKeyConstants.I18N_MESSAGES
                + locale + ":" + String.join(",", normalized);
    }

    private String buildLocalesKey() {
        return cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.I18N_LOCALES;
    }

    private Duration messageTtl() {
        return Duration.ofSeconds(cacheTtlProperties.getTtl().getConfig());
    }
}
