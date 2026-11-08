package com.icoolkj.platform.common.i18n;

import java.util.List;

/**
 * Supplies enabled locale codes from database configuration.
 */
public interface I18nLocaleProvider {

    List<String> getEnabledLocaleCodes();
}
