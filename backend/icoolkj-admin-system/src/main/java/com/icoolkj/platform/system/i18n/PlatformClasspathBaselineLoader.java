package com.icoolkj.platform.system.i18n;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** Loads Git-baseline platform i18n JSON from {@code classpath:i18n/platform/{locale}.json}. */
public final class PlatformClasspathBaselineLoader {

    private static final ResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();

    private PlatformClasspathBaselineLoader() {
    }

    public static Optional<String> loadI18nJson(String locale) {
        if (!StringUtils.hasText(locale)) {
            return Optional.empty();
        }
        return loadResource(String.format("classpath*:i18n/platform/%s.json", locale.trim()));
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
