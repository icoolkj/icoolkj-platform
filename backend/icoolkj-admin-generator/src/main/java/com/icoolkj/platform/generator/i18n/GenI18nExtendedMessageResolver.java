package com.icoolkj.platform.generator.i18n;

import com.icoolkj.platform.common.i18n.ExtendedMessageResolver;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.generator.service.GenI18nMessageService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Resolves backend keys {@code gen.{businessName}.*} from
 * Git classpath JSON only ({@code i18n/gen/{businessName}/{locale}.json}),
 * including Bean Validation, field labels, page texts, and audit maps.
 * DB gen_i18n_message is not used at runtime.
 */
@Component
public class GenI18nExtendedMessageResolver implements ExtendedMessageResolver {

    private static final String PREFIX = "gen.";

    private final GenI18nMessageService genI18nMessageService;

    public GenI18nExtendedMessageResolver(GenI18nMessageService genI18nMessageService) {
        this.genI18nMessageService = genI18nMessageService;
    }

    @Override
    public String resolve(String code, Object[] args, Locale locale) {
        if (!StringUtils.hasText(code) || !code.startsWith(PREFIX)) {
            return null;
        }
        String normalizedLocale = LocaleResolver.normalize(locale != null ? locale.toLanguageTag() : null);
        Map<String, String> messages = genI18nMessageService.getValidationMessageMap(normalizedLocale);
        String template = messages.get(code);
        if (!StringUtils.hasText(template)) {
            return null;
        }
        if (args == null || args.length == 0) {
            return template;
        }
        return MessageFormat.format(template, args);
    }
}
