package com.icoolkj.platform.infra.fileaccess;

import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.domain.InfraFileAccessPolicyEntity;
import com.icoolkj.platform.infra.mapper.InfraFileAccessPolicyMapper;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 策略注册表：内置兜底 {@code *} + {@code infra_file_access_policy} 表配置（同 pattern 以表为准）。
 * <p>Pinned patterns（Excel staging / OA 附件）始终以 builtin CUSTOM 为准，表行不可削弱。
 */
@Component
public class FileAccessPolicyRegistry {

    private static final Logger log = LoggerFactory.getLogger(FileAccessPolicyRegistry.class);

    private final InfraFileAccessPolicyMapper policyMapper;
    private final AtomicReference<List<FileAccessPolicy>> policies =
            new AtomicReference<>(FileAccessPolicyBuiltin.all());

    public FileAccessPolicyRegistry(InfraFileAccessPolicyMapper policyMapper) {
        this.policyMapper = policyMapper;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    /** 重新加载：先放内置兜底，再用表中启用行覆盖/追加；最后钉死 pinned patterns。 */
    public void refresh() {
        Map<String, FileAccessPolicy> merged = new LinkedHashMap<>(FileAccessPolicyBuiltin.byPattern());
        try {
            List<InfraFileAccessPolicyEntity> rows =
                    TenantScopeRunner.runIgnoringTenant(policyMapper::selectEnabled);
            if (rows != null) {
                for (InfraFileAccessPolicyEntity row : rows) {
                    FileAccessPolicy policy = toPolicy(row);
                    if (policy == null) {
                        continue;
                    }
                    String pattern = FileAccessPolicyMatcher.normalizePattern(policy.bizTypePattern());
                    if (FileAccessPolicyBuiltin.isPinnedPattern(pattern)) {
                        continue;
                    }
                    merged.put(pattern, policy);
                }
            }
        } catch (Exception ex) {
            log.warn("Load infra_file_access_policy failed, using builtin default only: {}", ex.getMessage());
        }
        // Fail-closed: always re-apply pinned builtins last.
        merged.putAll(FileAccessPolicyBuiltin.pinnedByPattern());
        List<FileAccessPolicy> list = new ArrayList<>(merged.values());
        policies.set(list);
        log.info("File access policies loaded: {} entries", list.size());
    }

    public FileAccessPolicy resolve(String bizType) {
        return FileAccessPolicyMatcher.resolve(bizType, policies.get());
    }

    public List<FileAccessPolicy> snapshot() {
        return List.copyOf(policies.get());
    }

    private static FileAccessPolicy toPolicy(InfraFileAccessPolicyEntity row) {
        if (row == null || !StringUtils.hasText(row.getBizTypePattern())) {
            return null;
        }
        int sortNo = row.getSortNo() == null ? 0 : row.getSortNo();
        return new FileAccessPolicy(
                row.getBizTypePattern().trim(),
                FileAccessAuthMode.fromCode(row.getAuthMode()),
                FileAccessTenantScope.fromCode(row.getTenantScope()),
                FileAccessVisibility.fromCode(row.getVisibility()),
                FileAccessPolicy.parseCsv(row.getAnonPurposes(), true),
                FileAccessPolicy.parseCsv(row.getPermAnyOf(), false),
                sortNo,
                row.getRemark());
    }
}
