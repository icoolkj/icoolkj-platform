package com.icoolkj.platform.system.i18n.service;

import java.util.List;
import java.util.Map;

public interface I18nCacheService {

    Map<String, String> getMessageMap(String locale, List<String> scopes);

    void putMessageMap(String locale, List<String> scopes, Map<String, String> messages);

    void evictMessages(String locale);

    void refreshAllMessages();

    List<String> getEnabledLocaleCodes();

    void putEnabledLocaleCodes(List<String> codes);

    void evictEnabledLocales();
}
