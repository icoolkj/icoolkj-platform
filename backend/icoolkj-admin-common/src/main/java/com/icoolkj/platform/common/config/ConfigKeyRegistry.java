package com.icoolkj.platform.common.config;

import com.icoolkj.platform.common.constant.ConfigKeys;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

/**
 * 平台已知配置键注册表（代码契约，非运行时自由 CRUD 字典）。
 * <p>写入时若键已登记则校验类型；未登记键仍允许（兼容自定义参数），但不进入 public bootstrap。
 */
public final class ConfigKeyRegistry {

    private static final Map<String, ConfigKeyMeta> REGISTRY;

    static {
        Map<String, ConfigKeyMeta> m = new LinkedHashMap<>();
        reg(m, ConfigKeys.LOGIN_MAX_FAIL_COUNT, ConfigKeyMeta.ConfigValueType.INT, true, false, false);
        reg(m, ConfigKeys.LOGIN_LOCK_MINUTES, ConfigKeyMeta.ConfigValueType.INT, true, false, false);
        reg(m, ConfigKeys.LOGIN_MAX_FAIL_COUNT_PER_IP, ConfigKeyMeta.ConfigValueType.INT, true, false, false);
        reg(m, ConfigKeys.LOGIN_IP_LOCK_MINUTES, ConfigKeyMeta.ConfigValueType.INT, true, false, false);
        reg(m, ConfigKeys.LOGIN_REQUIRE_CAPTCHA, ConfigKeyMeta.ConfigValueType.BOOL, true, false, false);
        reg(m, ConfigKeys.LOGIN_ALLOW_MULTI_LOGIN, ConfigKeyMeta.ConfigValueType.BOOL, true, false, false);
        reg(m, ConfigKeys.LOGIN_SINGLE_KICKOUT_OLDEST, ConfigKeyMeta.ConfigValueType.BOOL, true, false, false);

        reg(m, ConfigKeys.PASSWORD_EXPIRE_DAYS, ConfigKeyMeta.ConfigValueType.INT, true, false, false);
        reg(m, ConfigKeys.PASSWORD_FORCE_CHANGE_INITIAL, ConfigKeyMeta.ConfigValueType.BOOL, true, false, false);
        reg(m, ConfigKeys.PASSWORD_MIN_LENGTH, ConfigKeyMeta.ConfigValueType.INT, true, false, false);
        reg(m, ConfigKeys.PASSWORD_REQUIRE_UPPER, ConfigKeyMeta.ConfigValueType.BOOL, true, false, false);
        reg(m, ConfigKeys.PASSWORD_REQUIRE_LOWER, ConfigKeyMeta.ConfigValueType.BOOL, true, false, false);
        reg(m, ConfigKeys.PASSWORD_REQUIRE_NUMBER, ConfigKeyMeta.ConfigValueType.BOOL, true, false, false);
        reg(m, ConfigKeys.PASSWORD_REQUIRE_SPECIAL, ConfigKeyMeta.ConfigValueType.BOOL, true, false, false);
        reg(m, ConfigKeys.PASSWORD_HISTORY_COUNT, ConfigKeyMeta.ConfigValueType.INT, true, false, false);
        // 敏感：不进 public bootstrap；仍可租户覆盖（租户策略列表会脱敏展示）
        reg(m, ConfigKeys.PASSWORD_INITIAL, ConfigKeyMeta.ConfigValueType.STRING, true, false, true);

        reg(m, ConfigKeys.IP_DEFAULT_POLICY, ConfigKeyMeta.ConfigValueType.STRING, true, false, false);

        reg(m, ConfigKeys.MFA_ENABLED, ConfigKeyMeta.ConfigValueType.BOOL, true, false, false);
        reg(m, ConfigKeys.MFA_FORCE_ADMIN, ConfigKeyMeta.ConfigValueType.BOOL, true, false, false);

        reg(m, ConfigKeys.I18N_DEFAULT_LOCALE, ConfigKeyMeta.ConfigValueType.STRING, false, false, false);
        reg(m, ConfigKeys.I18N_SUPPORTED_LOCALES, ConfigKeyMeta.ConfigValueType.STRING, false, false, false);

        reg(m, ConfigKeys.AI_PROVIDER_API_KEY, ConfigKeyMeta.ConfigValueType.STRING, true, false, true);

        reg(m, ConfigKeys.BACKUP_OUTPUT_DIR, ConfigKeyMeta.ConfigValueType.STRING, false, false, false);
        reg(m, ConfigKeys.BACKUP_RETENTION_DAYS, ConfigKeyMeta.ConfigValueType.INT, false, false, false);
        reg(m, ConfigKeys.BACKUP_COMPRESS, ConfigKeyMeta.ConfigValueType.BOOL, false, false, false);

        reg(m, ConfigKeys.STORAGE_DEFAULT_PROVIDER, ConfigKeyMeta.ConfigValueType.STRING, false, false, false);
        reg(m, ConfigKeys.STORAGE_ENABLED_PROVIDERS, ConfigKeyMeta.ConfigValueType.STRING, false, false, false);
        reg(m, ConfigKeys.STORAGE_MAX_FILE_SIZE_MB, ConfigKeyMeta.ConfigValueType.INT, false, false, false);
        reg(m, ConfigKeys.STORAGE_ALLOWED_EXTENSIONS, ConfigKeyMeta.ConfigValueType.STRING, false, false, false);
        reg(m, ConfigKeys.STORAGE_BLOCKED_EXTENSIONS, ConfigKeyMeta.ConfigValueType.STRING, false, false, false);

        reg(m, ConfigKeys.AVATAR_MAX_SIZE_MB, ConfigKeyMeta.ConfigValueType.INT, false, false, false);
        reg(m, ConfigKeys.AVATAR_ALLOWED_EXTENSIONS, ConfigKeyMeta.ConfigValueType.STRING, false, false, false);
        reg(m, ConfigKeys.AVATAR_OUTPUT_SIZE, ConfigKeyMeta.ConfigValueType.INT, false, false, false);
        reg(m, ConfigKeys.AVATAR_THUMB_SM_SIZE, ConfigKeyMeta.ConfigValueType.INT, false, false, false);
        reg(m, ConfigKeys.AVATAR_THUMB_MD_SIZE, ConfigKeyMeta.ConfigValueType.INT, false, false, false);
        reg(m, ConfigKeys.AVATAR_CONTENT_SCAN_ENABLED, ConfigKeyMeta.ConfigValueType.BOOL, false, false, false);

        reg(m, ConfigKeys.WORKFLOW_APPROVE_QUICK_COMMENTS, ConfigKeyMeta.ConfigValueType.JSON, true, true, false);
        reg(m, ConfigKeys.WORKFLOW_NOTIFY_TEMPLATES, ConfigKeyMeta.ConfigValueType.JSON, true, false, false);

        REGISTRY = Collections.unmodifiableMap(m);
    }

    private ConfigKeyRegistry() {
    }

    public static Optional<ConfigKeyMeta> find(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return Optional.empty();
        }
        return Optional.ofNullable(REGISTRY.get(configKey.trim()));
    }

    public static Set<String> publicBootstrapKeys() {
        return REGISTRY.values().stream()
                .filter(ConfigKeyMeta::publicBootstrap)
                .map(ConfigKeyMeta::key)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static boolean isPublicBootstrapKey(String configKey) {
        return find(configKey).map(ConfigKeyMeta::publicBootstrap).orElse(false);
    }

    /**
     * Keys that tenants may override (ordered as registered).
     * Sensitive keys remain listed so admins can override them; callers should mask values for display.
     */
    public static java.util.List<ConfigKeyMeta> tenantOverridableMetas() {
        return REGISTRY.values().stream()
                .filter(ConfigKeyMeta::tenantOverridable)
                .toList();
    }

    public static boolean isSensitive(String configKey) {
        return find(configKey).map(ConfigKeyMeta::sensitive).orElse(false);
    }

    public static boolean isTenantOverridable(String configKey) {
        return find(configKey).map(ConfigKeyMeta::tenantOverridable).orElse(false);
    }

    /**
     * 已登记键：按类型校验；未登记键放行。
     */
    public static void validateValue(String configKey, String configValue) {
        ConfigKeyMeta meta = find(configKey).orElse(null);
        if (meta == null) {
            return;
        }
        String raw = configValue == null ? "" : configValue.trim();
        switch (meta.valueType()) {
            case INT -> {
                if (!raw.isEmpty()) {
                    try {
                        Integer.parseInt(raw);
                    } catch (NumberFormatException ex) {
                        throw new BusinessException("error.config.value_type_invalid", configKey, "int");
                    }
                }
            }
            case BOOL -> {
                if (!raw.isEmpty()
                        && !"true".equalsIgnoreCase(raw)
                        && !"false".equalsIgnoreCase(raw)
                        && !"Y".equalsIgnoreCase(raw)
                        && !"N".equalsIgnoreCase(raw)
                        && !YesNoEnum.isYes(raw)
                        && !YesNoEnum.isNo(raw)) {
                    throw new BusinessException("error.config.value_type_invalid", configKey, "bool");
                }
            }
            case JSON -> {
                if (!raw.isEmpty() && !(raw.startsWith("{") || raw.startsWith("["))) {
                    throw new BusinessException("error.config.value_type_invalid", configKey, "json");
                }
            }
            case STRING -> {
                // no-op
            }
            default -> {
            }
        }
    }

    private static void reg(Map<String, ConfigKeyMeta> map,
                            String key,
                            ConfigKeyMeta.ConfigValueType type,
                            boolean tenantOverridable,
                            boolean publicBootstrap,
                            boolean sensitive) {
        map.put(key, new ConfigKeyMeta(key, type, tenantOverridable, publicBootstrap, sensitive));
    }
}
