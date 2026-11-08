package com.icoolkj.platform.infra.fileprofile;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.storage.FileStoragePolicyUtils;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.domain.InfraFileProfileEntity;
import com.icoolkj.platform.infra.domain.InfraFileProfileRuleEntity;
import com.icoolkj.platform.infra.mapper.InfraFileProfileMapper;
import com.icoolkj.platform.infra.mapper.InfraFileProfileRuleMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 解析上传槽位：租户规则优先，回退平台；扩展名/大小不得超过平台边界。
 */
@Component
public class FileUploadProfileResolver {

    private final InfraFileProfileMapper profileMapper;
    private final InfraFileProfileRuleMapper ruleMapper;

    public FileUploadProfileResolver(InfraFileProfileMapper profileMapper,
                                     InfraFileProfileRuleMapper ruleMapper) {
        this.profileMapper = profileMapper;
        this.ruleMapper = ruleMapper;
    }

    public Optional<ResolvedFileUploadProfile> resolve(String tenantId, String profileCode) {
        if (!StringUtils.hasText(profileCode)) {
            return Optional.empty();
        }
        String code = profileCode.trim();
        String tid = StringUtils.hasText(tenantId) ? tenantId.trim() : TenantConstants.PLATFORM_TENANT_ID;

        return TenantScopeRunner.runIgnoringTenant(() -> {
            InfraFileProfileEntity profile = profileMapper.selectByTenantAndCode(tid, code);
            if (profile == null && !TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
                profile = profileMapper.selectByTenantAndCode(TenantConstants.PLATFORM_TENANT_ID, code);
            }
            if (profile == null || StatusEnum.isDisabled(profile.getStatus())) {
                return Optional.empty();
            }

            InfraFileProfileRuleEntity platformRule =
                    ruleMapper.selectByTenantAndCode(TenantConstants.PLATFORM_TENANT_ID, code);
            InfraFileProfileRuleEntity tenantRule = null;
            if (!TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
                tenantRule = ruleMapper.selectByTenantAndCode(tid, code);
            }

            InfraFileProfileRuleEntity effective = tenantRule != null ? tenantRule : platformRule;
            if (effective == null || StatusEnum.isDisabled(effective.getStatus())) {
                return Optional.of(profileOnly(profile, tid, tenantRule != null));
            }

            ResolvedFileUploadProfile merged = mergeWithinBounds(profile, platformRule, effective, tid, tenantRule != null);
            return Optional.of(merged);
        });
    }

    private static ResolvedFileUploadProfile profileOnly(InfraFileProfileEntity profile,
                                                         String tid,
                                                         boolean override) {
        return new ResolvedFileUploadProfile(
                profile.getProfileCode(),
                profile.getProfileName(),
                profile.getCategoryTag(),
                tid,
                override,
                List.of(),
                List.of(),
                false,
                0,
                1,
                0L,
                0L,
                0,
                profile.getStatus());
    }

    private ResolvedFileUploadProfile mergeWithinBounds(InfraFileProfileEntity profile,
                                                        InfraFileProfileRuleEntity platformRule,
                                                        InfraFileProfileRuleEntity effective,
                                                        String sourceTenantId,
                                                        boolean tenantOverride) {
        List<String> platformExts = parseCsvLower(platformRule == null ? null : platformRule.getAllowedExts());
        List<String> effectiveExts = parseCsvLower(effective.getAllowedExts());
        List<String> allowedExts = intersectOrTighten(platformExts, effectiveExts);

        List<String> platformTypes = parseCsvLower(platformRule == null ? null : platformRule.getAllowedContentTypes());
        List<String> effectiveTypes = parseCsvLower(effective.getAllowedContentTypes());
        List<String> allowedTypes = intersectOrTighten(platformTypes, effectiveTypes);

        long platformMax = platformRule == null || platformRule.getMaxSizeBytes() == null
                ? 0L : Math.max(0L, platformRule.getMaxSizeBytes());
        long effectiveMax = effective.getMaxSizeBytes() == null ? 0L : Math.max(0L, effective.getMaxSizeBytes());
        long maxSize = clampMaxSize(platformMax, effectiveMax);

        long platformMin = platformRule == null || platformRule.getMinSizeBytes() == null
                ? 0L : Math.max(0L, platformRule.getMinSizeBytes());
        long effectiveMin = effective.getMinSizeBytes() == null ? 0L : Math.max(0L, effective.getMinSizeBytes());
        long minSize = Math.max(platformMin, effectiveMin);
        if (maxSize > 0 && minSize > maxSize) {
            minSize = maxSize;
        }

        int platformMaxNum = platformRule == null || platformRule.getMaxNum() == null
                ? Integer.MAX_VALUE : Math.max(0, platformRule.getMaxNum());
        int effectiveMaxNum = effective.getMaxNum() == null ? 1 : Math.max(0, effective.getMaxNum());
        int maxNum = Math.min(platformMaxNum, effectiveMaxNum);

        int platformMinNum = platformRule == null || platformRule.getMinNum() == null
                ? 0 : Math.max(0, platformRule.getMinNum());
        int effectiveMinNum = effective.getMinNum() == null ? 0 : Math.max(0, effective.getMinNum());
        int minNum = Math.max(platformMinNum, effectiveMinNum);
        if (minNum > maxNum) {
            minNum = maxNum;
        }

        boolean platformRequired = platformRule != null && YesNoEnum.isYes(platformRule.getRequired());
        boolean required = platformRequired || YesNoEnum.isYes(effective.getRequired());

        return new ResolvedFileUploadProfile(
                profile.getProfileCode(),
                profile.getProfileName(),
                profile.getCategoryTag(),
                sourceTenantId,
                tenantOverride,
                allowedExts,
                allowedTypes,
                required,
                minNum,
                maxNum,
                minSize,
                maxSize,
                effective.getSortNo() == null ? 0 : effective.getSortNo(),
                effective.getStatus());
    }

    /** 平台白名单非空时，有效集合必须 ⊆ 平台；租户可收紧。 */
    public static List<String> intersectOrTighten(List<String> platform, List<String> effective) {
        if (platform == null || platform.isEmpty()) {
            return effective == null ? List.of() : List.copyOf(effective);
        }
        if (effective == null || effective.isEmpty()) {
            return List.copyOf(platform);
        }
        Set<String> platformSet = new LinkedHashSet<>(platform);
        List<String> out = new ArrayList<>();
        for (String item : effective) {
            if (platformSet.contains(item)) {
                out.add(item);
            }
        }
        return out.isEmpty() ? List.copyOf(platform) : List.copyOf(out);
    }

    public static long clampMaxSize(long platformMax, long effectiveMax) {
        if (platformMax <= 0) {
            return Math.max(0L, effectiveMax);
        }
        if (effectiveMax <= 0) {
            return platformMax;
        }
        return Math.min(platformMax, effectiveMax);
    }

    public static List<String> parseCsvLower(String csv) {
        if (!StringUtils.hasText(csv)) {
            return List.of();
        }
        List<String> fromPolicy = FileStoragePolicyUtils.splitCsv(csv);
        if (fromPolicy != null && !fromPolicy.isEmpty()) {
            return fromPolicy.stream()
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .toList();
        }
        List<String> out = new ArrayList<>();
        for (String part : csv.split(",")) {
            if (StringUtils.hasText(part)) {
                out.add(part.trim().toLowerCase(Locale.ROOT));
            }
        }
        return List.copyOf(out);
    }
}
