package com.icoolkj.platform.generator.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** Loads Git-baseline gen JSON from {@code classpath:i18n/gen/{businessName}/}. */
public final class GenClasspathBaselineLoader {

    private static final ResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();

    private GenClasspathBaselineLoader() {
    }

    public static Optional<String> loadI18nJson(String businessName, String locale) {
        if (!StringUtils.hasText(businessName) || !StringUtils.hasText(locale)) {
            return Optional.empty();
        }
        return loadResource(String.format("classpath*:i18n/gen/%s/%s.json", businessName.trim(), locale.trim()));
    }

    public static Optional<String> loadViewJson(String businessName) {
        if (!StringUtils.hasText(businessName)) {
            return Optional.empty();
        }
        return loadResource(String.format("classpath*:i18n/gen/%s/view.json", businessName.trim()));
    }

    private static Optional<String> loadResource(String locationPattern) {
        try {
            Resource[] resources = RESOLVER.getResources(locationPattern);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    return Optional.of(resource.getContentAsString(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException ignored) {
            // not on classpath
        }
        return Optional.empty();
    }
}
