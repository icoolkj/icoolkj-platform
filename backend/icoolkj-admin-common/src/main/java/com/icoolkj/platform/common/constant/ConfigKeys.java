package com.icoolkj.platform.common.constant;

/**
 * Well-known {@code sys_config.config_key} values.
 */
public final class ConfigKeys {

    public static final String LOGIN_MAX_FAIL_COUNT = "security.login.max-fail-count";
    public static final String LOGIN_LOCK_MINUTES = "security.login.lock-minutes";
    public static final String LOGIN_MAX_FAIL_COUNT_PER_IP = "security.login.max-fail-count-per-ip";
    public static final String LOGIN_IP_LOCK_MINUTES = "security.login.ip-lock-minutes";
    public static final String LOGIN_REQUIRE_CAPTCHA = "security.login.require-captcha";
    public static final String LOGIN_ALLOW_MULTI_LOGIN = "security.login.allow-multi-login";
    public static final String LOGIN_SINGLE_KICKOUT_OLDEST = "security.login.single-login-kickout-oldest";

    public static final String PASSWORD_EXPIRE_DAYS = "security.password.expire-days";
    public static final String PASSWORD_FORCE_CHANGE_INITIAL = "security.password.force-change-initial";
    public static final String PASSWORD_MIN_LENGTH = "security.password.min-length";
    public static final String PASSWORD_REQUIRE_UPPER = "security.password.require-upper";
    public static final String PASSWORD_REQUIRE_LOWER = "security.password.require-lower";
    public static final String PASSWORD_REQUIRE_NUMBER = "security.password.require-number";
    public static final String PASSWORD_REQUIRE_SPECIAL = "security.password.require-special";
    public static final String PASSWORD_HISTORY_COUNT = "security.password.history-count";
    /** 新建用户留空 / 管理员重置密码时使用的标准初始明文密码（敏感） */
    public static final String PASSWORD_INITIAL = "security.password.initial";

    public static final String IP_DEFAULT_POLICY = "security.ip.default-policy";

    /** 本租户是否启用 MFA 能力（关闭则不校验/不强制绑定） */
    public static final String MFA_ENABLED = "security.mfa.enabled";
    /** 对超管/超级角色/三权角色强制绑定 MFA */
    public static final String MFA_FORCE_ADMIN = "security.mfa.force-admin";

    public static final String I18N_DEFAULT_LOCALE = "system.i18n.default-locale";
    public static final String I18N_SUPPORTED_LOCALES = "system.i18n.supported-locales";

    /** 用于加密 tool_ai_provider.api_key_cipher（非 sys_config 行，仅走 SensitiveConfigCrypto） */
    public static final String AI_PROVIDER_API_KEY = "ai.provider.api-key";

    public static final String BACKUP_OUTPUT_DIR = "infra.backup.output-dir";
    public static final String BACKUP_RETENTION_DAYS = "infra.backup.retention-days";
    public static final String BACKUP_COMPRESS = "infra.backup.compress";

    public static final String STORAGE_DEFAULT_PROVIDER = "infra.storage.default-provider";
    public static final String STORAGE_ENABLED_PROVIDERS = "infra.storage.enabled-providers";
    public static final String STORAGE_MAX_FILE_SIZE_MB = "infra.storage.max-file-size-mb";
    public static final String STORAGE_ALLOWED_EXTENSIONS = "infra.storage.allowed-extensions";
    public static final String STORAGE_BLOCKED_EXTENSIONS = "infra.storage.blocked-extensions";

    public static final String AVATAR_MAX_SIZE_MB = "infra.avatar.max-size-mb";
    public static final String AVATAR_ALLOWED_EXTENSIONS = "infra.avatar.allowed-extensions";
    public static final String AVATAR_OUTPUT_SIZE = "infra.avatar.output-size";
    public static final String AVATAR_THUMB_SM_SIZE = "infra.avatar.thumb-sm-size";
    public static final String AVATAR_THUMB_MD_SIZE = "infra.avatar.thumb-md-size";
    public static final String AVATAR_CONTENT_SCAN_ENABLED = "infra.avatar.content-scan-enabled";

    /** Approval quick phrases JSON (pass/reject + per-locale labels); tenant whole-package override. */
    public static final String WORKFLOW_APPROVE_QUICK_COMMENTS = "workflow.approve.quick-comments";

    /**
     * Workflow site-message notify templates JSON（按 eventType 覆盖 title/content 模式）。
     * 占位符：{@code {eventLabel}} {@code {bizTitle}} {@code {instanceId}} {@code {title}}。
     * 缺省/空则回落 MessageSource {@code notify.workflow.*}。
     */
    public static final String WORKFLOW_NOTIFY_TEMPLATES = "workflow.notify.templates";

    private ConfigKeys() {
    }
}
