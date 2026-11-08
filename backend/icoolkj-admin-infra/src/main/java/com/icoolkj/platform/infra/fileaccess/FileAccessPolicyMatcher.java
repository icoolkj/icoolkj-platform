package com.icoolkj.platform.infra.fileaccess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.util.StringUtils;

/**
 * biz_type → 策略匹配：最长 pattern 优先，同分按 sort_no 升序。
 */
public final class FileAccessPolicyMatcher {

    private FileAccessPolicyMatcher() {
    }

    public static FileAccessPolicy resolve(String bizType, List<FileAccessPolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            return FileAccessPolicyBuiltin.DEFAULT;
        }
        String normalized = StringUtils.hasText(bizType) ? bizType.trim() : "";
        List<Scored> matched = new ArrayList<>();
        for (FileAccessPolicy policy : policies) {
            int score = matchScore(normalized, policy.bizTypePattern());
            if (score >= 0) {
                matched.add(new Scored(policy, score));
            }
        }
        if (matched.isEmpty()) {
            return FileAccessPolicyBuiltin.DEFAULT;
        }
        matched.sort(Comparator
                .comparingInt(Scored::score).reversed()
                .thenComparingInt(s -> s.policy().sortNo())
                .thenComparing(s -> s.policy().bizTypePattern()));
        return matched.get(0).policy();
    }

    /**
     * @return 匹配分（越大越具体）；不匹配返回 -1。精确=1000+len；前缀=len；通配 *=0
     */
    static int matchScore(String bizType, String pattern) {
        if (!StringUtils.hasText(pattern)) {
            return -1;
        }
        String p = pattern.trim();
        if ("*".equals(p)) {
            return 0;
        }
        if (p.endsWith("*")) {
            String prefix = p.substring(0, p.length() - 1);
            if (!StringUtils.hasText(prefix)) {
                return 0;
            }
            if (bizType.regionMatches(true, 0, prefix, 0, prefix.length())) {
                return prefix.length();
            }
            return -1;
        }
        if (bizType.equalsIgnoreCase(p)) {
            return 1000 + p.length();
        }
        return -1;
    }

    public static String normalizePattern(String pattern) {
        if (!StringUtils.hasText(pattern)) {
            return "*";
        }
        return pattern.trim().toLowerCase(Locale.ROOT);
    }

    private record Scored(FileAccessPolicy policy, int score) {
    }
}
