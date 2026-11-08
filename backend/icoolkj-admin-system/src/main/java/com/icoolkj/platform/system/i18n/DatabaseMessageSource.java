package com.icoolkj.platform.system.i18n;

import com.icoolkj.platform.system.i18n.service.I18nQueryService;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Database-backed {@link MessageSource} for backend error/validation messages.
 */
@Component
public class DatabaseMessageSource implements MessageSource {

    private final I18nQueryService i18nQueryService;

    public DatabaseMessageSource(I18nQueryService i18nQueryService) {
        this.i18nQueryService = i18nQueryService;
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        String resolved = i18nQueryService.resolve(code, args, locale);
        if (code.equals(resolved) && defaultMessage != null) {
            return defaultMessage;
        }
        return resolved;
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return i18nQueryService.resolve(code, args, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();
        if (codes == null || codes.length == 0) {
            throw new NoSuchMessageException("");
        }
        for (String code : codes) {
            String message = getMessage(code, resolvable.getArguments(), locale);
            if (!code.equals(message)) {
                return message;
            }
        }
        if (resolvable.getDefaultMessage() != null) {
            return resolvable.getDefaultMessage();
        }
        throw new NoSuchMessageException(codes[0], locale);
    }
}
