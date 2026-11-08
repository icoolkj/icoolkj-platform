package com.icoolkj.platform.common.constant;

import java.util.Locale;
import java.util.Set;

/**
 * 字典类型拉取作用域（配置下拉用，不等于运行时 getDict）。
 * <p>OA 发起/字段映射：只露出业务选项池，避免全系统字典混进动态配置。
 */
public final class DictTypeScope {

    /** OA 动态表单字段可选字典 */
    public static final String OA = "oa";

    /** OA 业务选项字典前缀 */
    public static final String OA_PREFIX = "oa_";

    /** 允许 OA 表单复用的系统字典（非 oa_ 前缀） */
    public static final Set<String> OA_EXTRA_ALLOWLIST = Set.of("sys_bool");

    /**
     * OA 运行态/管理态字典：会出现在 oa_ 前缀下，但不作为动态表单字段绑定项。
     */
    public static final Set<String> OA_FORM_EXCLUDE = Set.of(
            "oa_approve_status",
            "oa_enable_status"
    );

    private DictTypeScope() {
    }

    public static boolean matches(String scope, String dictType) {
        if (dictType == null || dictType.isBlank()) {
            return false;
        }
        String type = dictType.trim();
        if (scope == null || scope.isBlank()) {
            return true;
        }
        String normalized = scope.trim().toLowerCase(Locale.ROOT);
        if (OA.equals(normalized)) {
            return matchesOaFormDict(type);
        }
        return true;
    }

    public static boolean matchesOaFormDict(String dictType) {
        if (OA_FORM_EXCLUDE.contains(dictType)) {
            return false;
        }
        if (OA_EXTRA_ALLOWLIST.contains(dictType)) {
            return true;
        }
        return dictType.startsWith(OA_PREFIX);
    }
}
