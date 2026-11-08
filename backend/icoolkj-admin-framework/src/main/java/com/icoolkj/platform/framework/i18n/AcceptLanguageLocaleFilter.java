package com.icoolkj.platform.framework.i18n;

import com.icoolkj.platform.common.i18n.I18nLocaleProvider;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

/**
 * Binds {@link LocaleContextHolder} from {@code Accept-Language} for MessageSource resolution.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AcceptLanguageLocaleFilter extends OncePerRequestFilter {

    private final ObjectProvider<I18nLocaleProvider> i18nLocaleProvider;

    public AcceptLanguageLocaleFilter(ObjectProvider<I18nLocaleProvider> i18nLocaleProvider) {
        this.i18nLocaleProvider = i18nLocaleProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        I18nLocaleProvider provider = i18nLocaleProvider.getIfAvailable();
        String tag = provider == null
                ? LocaleResolver.resolve(request.getHeader("Accept-Language"))
                : LocaleResolver.resolve(
                        request.getHeader("Accept-Language"),
                        LocaleResolver.LOCALE_ZH_CN,
                        provider.getEnabledLocaleCodes());
        LocaleContextHolder.setLocale(Locale.forLanguageTag(tag));
        try {
            filterChain.doFilter(request, response);
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }
}
