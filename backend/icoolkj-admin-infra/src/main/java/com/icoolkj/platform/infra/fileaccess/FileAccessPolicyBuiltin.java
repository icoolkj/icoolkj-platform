package com.icoolkj.platform.infra.fileaccess;

import com.icoolkj.platform.infra.excel.ExcelExportAsyncConstants;
import com.icoolkj.platform.infra.excel.ExcelImportAsyncConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;

/**
 * 代码侧保留：兜底 {@code *}，以及不可被表配置削弱的钉死策略（Excel staging / OA 附件）。
 * <p>其余业务策略（brand / notice / banner / avatar / demo 等）以
 * {@code infra_file_access_policy} 表 + 管理端为准。
 */
public final class FileAccessPolicyBuiltin {

    public static final String PATTERN_DEFAULT = "*";
    /** OA 实例附件上传 / 引用 biz_type（与 OaInstanceFileConstants.BIZ_TYPE 一致）。 */
    public static final String PATTERN_OA_INSTANCE = "oa_instance";

    /** 表未加载或空库时的安全兜底：已登录 + 仅本租户（无跨租户旁路）。 */
    public static final FileAccessPolicy DEFAULT = new FileAccessPolicy(
            PATTERN_DEFAULT,
            FileAccessAuthMode.SESSION,
            FileAccessTenantScope.OWN,
            FileAccessVisibility.ALWAYS,
            Set.of(),
            Set.of(),
            9999,
            null);

    /** Async Excel import staging — submitter-only via ExcelStagingFileVisibilityChecker. */
    public static final FileAccessPolicy EXCEL_IMPORT = new FileAccessPolicy(
            ExcelImportAsyncConstants.BIZ_TYPE,
            FileAccessAuthMode.SESSION,
            FileAccessTenantScope.OWN,
            FileAccessVisibility.CUSTOM,
            Set.of(),
            Set.of(),
            5,
            null);

    /** Async Excel export staging — submitter-only via ExcelStagingFileVisibilityChecker. */
    public static final FileAccessPolicy EXCEL_EXPORT = new FileAccessPolicy(
            ExcelExportAsyncConstants.BIZ_TYPE,
            FileAccessAuthMode.SESSION,
            FileAccessTenantScope.OWN,
            FileAccessVisibility.CUSTOM,
            Set.of(),
            Set.of(),
            5,
            null);

    /**
     * OA instance attachments — starter / workflow participants / DataScope-readable via
     * OaInstanceFileVisibilityChecker (oa module).
     */
    public static final FileAccessPolicy OA_INSTANCE = new FileAccessPolicy(
            PATTERN_OA_INSTANCE,
            FileAccessAuthMode.SESSION,
            FileAccessTenantScope.OWN,
            FileAccessVisibility.CUSTOM,
            Set.of(),
            Set.of(),
            50,
            null);

    private static final Map<String, FileAccessPolicy> BY_PATTERN;
    private static final Map<String, FileAccessPolicy> PINNED_BY_PATTERN;

    static {
        Map<String, FileAccessPolicy> pinned = new LinkedHashMap<>();
        pinned.put(FileAccessPolicyMatcher.normalizePattern(EXCEL_IMPORT.bizTypePattern()), EXCEL_IMPORT);
        pinned.put(FileAccessPolicyMatcher.normalizePattern(EXCEL_EXPORT.bizTypePattern()), EXCEL_EXPORT);
        pinned.put(FileAccessPolicyMatcher.normalizePattern(OA_INSTANCE.bizTypePattern()), OA_INSTANCE);
        PINNED_BY_PATTERN = Collections.unmodifiableMap(pinned);

        Map<String, FileAccessPolicy> map = new LinkedHashMap<>(pinned);
        map.put(FileAccessPolicyMatcher.normalizePattern(PATTERN_DEFAULT), DEFAULT);
        BY_PATTERN = Collections.unmodifiableMap(map);
    }

    private FileAccessPolicyBuiltin() {
    }

    public static List<FileAccessPolicy> all() {
        return new ArrayList<>(BY_PATTERN.values());
    }

    public static Map<String, FileAccessPolicy> byPattern() {
        return BY_PATTERN;
    }

    /** Patterns re-applied after DB merge so table rows cannot weaken them. */
    public static Map<String, FileAccessPolicy> pinnedByPattern() {
        return PINNED_BY_PATTERN;
    }

    public static boolean isPinnedPattern(String pattern) {
        if (!StringUtils.hasText(pattern)) {
            return false;
        }
        return PINNED_BY_PATTERN.containsKey(
                FileAccessPolicyMatcher.normalizePattern(pattern.trim().toLowerCase(Locale.ROOT)));
    }
}
