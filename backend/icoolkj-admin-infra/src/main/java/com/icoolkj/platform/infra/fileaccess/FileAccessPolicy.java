package com.icoolkj.platform.infra.fileaccess;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 一条已解析的文件访问策略（来自代码内置或 {@code infra_file_access_policy}）。
 */
public record FileAccessPolicy(
        String bizTypePattern,
        FileAccessAuthMode authMode,
        FileAccessTenantScope tenantScope,
        FileAccessVisibility visibility,
        Set<String> anonPurposes,
        Set<String> permAnyOf,
        int sortNo,
        String remark
) {

    public FileAccessPolicy {
        anonPurposes = anonPurposes == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(anonPurposes));
        permAnyOf = permAnyOf == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(permAnyOf));
    }

    public boolean allowsAnonPurpose(FileAccessPurpose purpose) {
        if (purpose == null || anonPurposes.isEmpty()) {
            return false;
        }
        return anonPurposes.contains(purpose.name());
    }

    public static Set<String> parseCsv(String csv) {
        return parseCsv(csv, false);
    }

    /** @param upper whether to normalize tokens to upper-case (purpose codes); permissions keep original case */
    public static Set<String> parseCsv(String csv, boolean upper) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (String part : csv.split(",")) {
            if (part != null && !part.isBlank()) {
                String token = part.trim();
                out.add(upper ? token.toUpperCase(Locale.ROOT) : token);
            }
        }
        return out;
    }
}
