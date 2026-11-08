package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.infra.domain.InfraFileAccessPolicyEntity;
import com.icoolkj.platform.infra.dto.InfraFileAccessPolicyUpsertRequest;
import com.icoolkj.platform.infra.fileaccess.FileAccessAuthMode;
import com.icoolkj.platform.infra.fileaccess.FileAccessPolicy;
import com.icoolkj.platform.infra.fileaccess.FileAccessPolicyBuiltin;
import com.icoolkj.platform.infra.fileaccess.FileAccessPolicyRegistry;
import com.icoolkj.platform.infra.fileaccess.FileAccessTenantScope;
import com.icoolkj.platform.infra.fileaccess.FileAccessVisibility;
import com.icoolkj.platform.infra.mapper.InfraFileAccessPolicyMapper;
import com.icoolkj.platform.infra.service.InfraFileAccessPolicyAdminService;
import com.icoolkj.platform.infra.vo.InfraFileAccessPolicyVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class InfraFileAccessPolicyAdminServiceImpl implements InfraFileAccessPolicyAdminService {

    private final InfraFileAccessPolicyMapper policyMapper;
    private final FileAccessPolicyRegistry policyRegistry;

    public InfraFileAccessPolicyAdminServiceImpl(InfraFileAccessPolicyMapper policyMapper,
                                                 FileAccessPolicyRegistry policyRegistry) {
        this.policyMapper = policyMapper;
        this.policyRegistry = policyRegistry;
    }

    @Override
    public List<InfraFileAccessPolicyVO> list() {
        requirePlatformOperator();
        List<InfraFileAccessPolicyEntity> rows = policyMapper.selectAll();
        List<InfraFileAccessPolicyVO> out = new ArrayList<>();
        if (rows != null) {
            for (InfraFileAccessPolicyEntity row : rows) {
                out.add(toVo(row));
            }
        }
        return out;
    }

    @Override
    public List<InfraFileAccessPolicyVO> listEffective() {
        requirePlatformOperator();
        return policyRegistry.snapshot().stream().map(this::toVoFromRuntime).collect(Collectors.toList());
    }

    @Override
    public InfraFileAccessPolicyVO getById(String id) {
        requirePlatformOperator();
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("error.validation.required", "fields.id");
        }
        InfraFileAccessPolicyEntity row = policyMapper.selectById(id.trim());
        if (row == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.fileAccessPolicy");
        }
        return toVo(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsert(InfraFileAccessPolicyUpsertRequest request) {
        requirePlatformOperator();
        String pattern = normalizePattern(request.getBizTypePattern());
        assertNotPinned(pattern);
        String authMode = FileAccessAuthMode.fromCode(request.getAuthMode()).name();
        String tenantScope = FileAccessTenantScope.fromCode(request.getTenantScope()).name();
        String visibility = FileAccessVisibility.fromCode(request.getVisibility()).name();
        String enabled = normalizeEnabled(request.getEnabled());
        LocalDateTime now = LocalDateTime.now();
        String operator = AuditFieldHelper.operatorId();

        InfraFileAccessPolicyEntity existing = null;
        if (StringUtils.hasText(request.getId())) {
            existing = policyMapper.selectById(request.getId().trim());
            if (existing == null) {
                throw new BusinessException("error.entity.not_found", "entity.name.fileAccessPolicy");
            }
            assertNotPinned(existing.getBizTypePattern());
        } else {
            existing = policyMapper.selectByPattern(pattern);
            if (existing != null) {
                assertNotPinned(existing.getBizTypePattern());
            }
        }

        InfraFileAccessPolicyEntity dup = policyMapper.selectByPattern(pattern);
        if (dup != null && (existing == null || !dup.getId().equals(existing.getId()))) {
            throw new BusinessException("error.file.access_policy_pattern_duplicate");
        }

        if (existing == null) {
            InfraFileAccessPolicyEntity entity = new InfraFileAccessPolicyEntity();
            entity.setId(IdGeneratorUtils.nextIdStr());
            entity.setBizTypePattern(pattern);
            entity.setAuthMode(authMode);
            entity.setTenantScope(tenantScope);
            entity.setVisibility(visibility);
            entity.setAnonPurposes(blankToNull(request.getAnonPurposes()));
            entity.setPermAnyOf(blankToNull(request.getPermAnyOf()));
            entity.setSortNo(request.getSortNo() == null ? 100 : request.getSortNo());
            entity.setEnabled(enabled);
            entity.setRemark(blankToNull(request.getRemark()));
            entity.setCreatedBy(operator);
            entity.setCreatedAt(now);
            entity.setUpdatedBy(operator);
            entity.setUpdatedAt(now);
            entity.setDeleted(0);
            policyMapper.insert(entity);
        } else {
            existing.setBizTypePattern(pattern);
            existing.setAuthMode(authMode);
            existing.setTenantScope(tenantScope);
            existing.setVisibility(visibility);
            existing.setAnonPurposes(blankToNull(request.getAnonPurposes()));
            existing.setPermAnyOf(blankToNull(request.getPermAnyOf()));
            if (request.getSortNo() != null) {
                existing.setSortNo(request.getSortNo());
            }
            existing.setEnabled(enabled);
            if (request.getRemark() != null) {
                existing.setRemark(blankToNull(request.getRemark()));
            }
            existing.setUpdatedBy(operator);
            existing.setUpdatedAt(now);
            policyMapper.updateById(existing);
        }
        policyRegistry.refresh();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        requirePlatformOperator();
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("error.validation.required", "fields.id");
        }
        InfraFileAccessPolicyEntity row = policyMapper.selectById(id.trim());
        if (row == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.fileAccessPolicy");
        }
        assertNotPinned(row.getBizTypePattern());
        policyMapper.logicalDeleteById(row.getId(), AuditFieldHelper.operatorId(), LocalDateTime.now());
        policyRegistry.refresh();
    }

    @Override
    public void refreshRegistry() {
        requirePlatformOperator();
        policyRegistry.refresh();
    }

    private static void assertNotPinned(String pattern) {
        if (FileAccessPolicyBuiltin.isPinnedPattern(pattern)) {
            throw new BusinessException("error.file.access_policy_pinned", pattern == null ? "" : pattern.trim());
        }
    }

    private static void requirePlatformOperator() {
        if (!TenantConstants.PLATFORM_TENANT_ID.equals(SecurityContextHelper.getTenantId())) {
            throw new BusinessException("error.forbidden");
        }
    }

    private static String normalizePattern(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new BusinessException("error.validation.required", "fields.bizTypePattern");
        }
        return raw.trim();
    }

    /** 归一化为 {@code 1}/{@code 0}；非法值回落为启用。 */
    private static String normalizeEnabled(String raw) {
        String enabled = StringUtils.hasText(raw) ? raw.trim() : CommonConstants.YES;
        if (!YesNoEnum.isNo(enabled) && !YesNoEnum.isYes(enabled)) {
            return CommonConstants.YES;
        }
        return enabled;
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static InfraFileAccessPolicyVO toVo(InfraFileAccessPolicyEntity row) {
        InfraFileAccessPolicyVO vo = new InfraFileAccessPolicyVO();
        vo.setId(row.getId());
        vo.setBizTypePattern(row.getBizTypePattern());
        vo.setAuthMode(row.getAuthMode());
        vo.setTenantScope(row.getTenantScope());
        vo.setVisibility(row.getVisibility());
        vo.setAnonPurposes(row.getAnonPurposes());
        vo.setPermAnyOf(row.getPermAnyOf());
        vo.setSortNo(row.getSortNo());
        vo.setEnabled(row.getEnabled());
        vo.setRemark(row.getRemark());
        vo.setUpdatedAt(row.getUpdatedAt());
        vo.setPinned(FileAccessPolicyBuiltin.isPinnedPattern(row.getBizTypePattern()));
        return vo;
    }

    private InfraFileAccessPolicyVO toVoFromRuntime(FileAccessPolicy policy) {
        InfraFileAccessPolicyVO vo = new InfraFileAccessPolicyVO();
        vo.setBizTypePattern(policy.bizTypePattern());
        vo.setAuthMode(policy.authMode() == null ? null : policy.authMode().name());
        vo.setTenantScope(policy.tenantScope() == null ? null : policy.tenantScope().name());
        vo.setVisibility(policy.visibility() == null ? null : policy.visibility().name());
        vo.setAnonPurposes(join(policy.anonPurposes()));
        vo.setPermAnyOf(join(policy.permAnyOf()));
        vo.setSortNo(policy.sortNo());
        vo.setEnabled(CommonConstants.YES);
        vo.setRemark(policy.remark());
        vo.setPinned(FileAccessPolicyBuiltin.isPinnedPattern(policy.bizTypePattern()));
        return vo;
    }

    private static String join(java.util.Set<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join(",", values);
    }
}
