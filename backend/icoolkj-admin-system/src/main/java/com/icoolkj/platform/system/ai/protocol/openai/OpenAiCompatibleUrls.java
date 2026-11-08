package com.icoolkj.platform.system.ai.protocol.openai;

import com.icoolkj.platform.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Locale;

public final class OpenAiCompatibleUrls {

    private OpenAiCompatibleUrls() {
    }

    public static String trimTrailingSlash(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "";
        }
        String url = baseUrl.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String requireApiRoot(String baseUrl) {
        String url = trimTrailingSlash(baseUrl);
        if (!StringUtils.hasText(url)) {
            throw new BusinessException("error.ai.base_url_required");
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("error.ai.base_url_invalid", url);
        }
        String scheme = uri.getScheme();
        if (scheme == null
                || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
                || !StringUtils.hasText(uri.getHost())) {
            throw new BusinessException("error.ai.base_url_invalid", url);
        }
        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        for (String suffix : OpenAiCompatibleEndpoints.RESOURCE_PATHS) {
            if (path.endsWith(suffix)) {
                throw new BusinessException("error.ai.base_url_must_be_api_root", url);
            }
        }
        return url;
    }
}
