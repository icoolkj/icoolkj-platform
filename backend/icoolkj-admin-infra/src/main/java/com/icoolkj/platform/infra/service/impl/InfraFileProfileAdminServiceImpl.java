package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.domain.InfraFileCatalogEntity;
import com.icoolkj.platform.infra.domain.InfraFileCatalogItemEntity;
import com.icoolkj.platform.infra.domain.InfraFileProfileEntity;
import com.icoolkj.platform.infra.domain.InfraFileProfileRuleEntity;
import com.icoolkj.platform.infra.dto.InfraFileCatalogItemUpsertRequest;
import com.icoolkj.platform.infra.dto.InfraFileCatalogUpsertRequest;
import com.icoolkj.platform.infra.dto.InfraFileProfileBatchDeleteRequest;
import com.icoolkj.platform.infra.dto.InfraFileProfileUpsertRequest;
import com.icoolkj.platform.infra.fileprofile.FileUploadProfileResolver;
import com.icoolkj.platform.infra.fileprofile.ResolvedFileUploadProfile;
import com.icoolkj.platform.infra.mapper.InfraFileCatalogItemMapper;
import com.icoolkj.platform.infra.mapper.InfraFileCatalogMapper;
import com.icoolkj.platform.infra.mapper.InfraFileProfileMapper;
import com.icoolkj.platform.infra.mapper.InfraFileProfileRuleMapper;
import com.icoolkj.platform.infra.service.InfraFileProfileAdminService;
import com.icoolkj.platform.infra.support.ChromeRolesJsonHelper;
import com.icoolkj.platform.infra.support.NameI18nJsonHelper;
import com.icoolkj.platform.infra.vo.InfraFileCatalogItemVO;
import com.icoolkj.platform.infra.vo.InfraFileCatalogVO;
import com.icoolkj.platform.infra.vo.InfraFileProfileVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class InfraFileProfileAdminServiceImpl implements InfraFileProfileAdminService {

    private final InfraFileProfileMapper profileMapper;
    private final InfraFileProfileRuleMapper ruleMapper;
    private final InfraFileCatalogMapper catalogMapper;
    private final InfraFileCatalogItemMapper catalogItemMapper;
    private final FileUploadProfileResolver profileResolver;

    public InfraFileProfileAdminServiceImpl(InfraFileProfileMapper profileMapper,
                                            InfraFileProfileRuleMapper ruleMapper,
                                            InfraFileCatalogMapper catalogMapper,
                                            InfraFileCatalogItemMapper catalogItemMapper,
                                            FileUploadProfileResolver profileResolver) {
        this.profileMapper = profileMapper;
        this.ruleMapper = ruleMapper;
        this.catalogMapper = catalogMapper;
        this.catalogItemMapper = catalogItemMapper;
        this.profileResolver = profileResolver;
    }

    @Override
    public List<InfraFileProfileVO> list(String categoryTag, String keyword, String targetTenantId) {
        String tid = resolveTargetTenantId(targetTenantId);
        List<InfraFileProfileEntity> profiles = TenantScopeRunner.runIgnoringTenant(() ->
                profileMapper.selectByTenant(TenantConstants.PLATFORM_TENANT_ID, categoryTag, keyword));
        List<InfraFileProfileVO> rows = new ArrayList<>();
        for (InfraFileProfileEntity profile : profiles) {
            rows.add(toVo(profile, tid));
        }
        return rows;
    }

    @Override
    public InfraFileProfileVO resolve(String profileCode, String targetTenantId) {
        if (!StringUtils.hasText(profileCode)) {
            throw new BusinessException("error.validation.required", "fields.profileCode");
        }
        String tid = resolveTargetTenantId(targetTenantId);
        Optional<ResolvedFileUploadProfile> resolved = profileResolver.resolve(tid, profileCode.trim());
        if (resolved.isEmpty()) {
            throw new BusinessException("error.entity.not_found", "entity.name.fileProfile");
        }
        InfraFileProfileEntity profile = TenantScopeRunner.runIgnoringTenant(() -> {
            InfraFileProfileEntity p = profileMapper.selectByTenantAndCode(tid, profileCode.trim());
            if (p == null) {
                p = profileMapper.selectByTenantAndCode(TenantConstants.PLATFORM_TENANT_ID, profileCode.trim());
            }
            return p;
        });
        InfraFileProfileVO vo = toVoFromResolved(resolved.get());
        if (profile != null) {
            vo.setProfileNameI18n(NameI18nJsonHelper.parse(profile.getProfileNameI18n()));
            vo.setRemark(profile.getRemark());
            vo.setStatus(profile.getStatus());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsert(InfraFileProfileUpsertRequest request) {
        String tid = resolveTargetTenantId(request.getTargetTenantId());
        String code = request.getProfileCode().trim();

        if (TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
            upsertPlatformProfileAndRule(request, code);
            return;
        }

        // 租户：仅允许覆盖规则，且不得超出平台边界
        InfraFileProfileEntity platformProfile = TenantScopeRunner.runIgnoringTenant(() ->
                profileMapper.selectByTenantAndCode(TenantConstants.PLATFORM_TENANT_ID, code));
        if (platformProfile == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.fileProfile");
        }
        InfraFileProfileRuleEntity platformRule = TenantScopeRunner.runIgnoringTenant(() ->
                ruleMapper.selectByTenantAndCode(TenantConstants.PLATFORM_TENANT_ID, code));
        assertWithinPlatformBounds(platformRule, request);
        upsertTenantRule(tid, code, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetRuleOverride(String profileCode, String targetTenantId) {
        String tid = resolveTargetTenantId(targetTenantId);
        if (TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
            throw new BusinessException("error.file.profile_platform_reset_forbidden");
        }
        if (!StringUtils.hasText(profileCode)) {
            throw new BusinessException("error.validation.required", "fields.profileCode");
        }
        TenantScopeRunner.runIgnoringTenant(() ->
                ruleMapper.deleteByTenantAndCode(tid, profileCode.trim()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProfile(String profileCode, String targetTenantId) {
        String tid = resolveTargetTenantId(targetTenantId);
        requirePlatformOperatorForStructure(tid);
        if (!StringUtils.hasText(profileCode)) {
            throw new BusinessException("error.validation.required", "fields.profileCode");
        }
        String code = profileCode.trim();
        TenantScopeRunner.runIgnoringTenant(() -> {
            InfraFileProfileEntity profile = profileMapper.selectByTenantAndCode(tid, code);
            if (profile == null) {
                throw new BusinessException("error.entity.not_found", "entity.name.fileProfile");
            }
            profileMapper.logicalDeleteById(profile.getId());
            InfraFileProfileRuleEntity rule = ruleMapper.selectByTenantAndCode(tid, code);
            if (rule != null) {
                ruleMapper.logicalDeleteById(rule.getId());
            }
            // 清理本租户目录中对该槽位的引用
            for (InfraFileCatalogEntity catalog : catalogMapper.selectByTenant(tid)) {
                List<InfraFileCatalogItemEntity> items =
                        catalogItemMapper.selectByTenantAndCatalog(tid, catalog.getCatalogCode());
                for (InfraFileCatalogItemEntity item : items) {
                    if (code.equals(item.getProfileCode())) {
                        catalogItemMapper.logicalDeleteById(item.getId());
                    }
                }
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteProfiles(InfraFileProfileBatchDeleteRequest request) {
        if (request == null || request.getProfileCodes() == null || request.getProfileCodes().isEmpty()) {
            throw new BusinessException("error.validation.required", "fields.profileCode");
        }
        for (String code : request.getProfileCodes()) {
            if (StringUtils.hasText(code)) {
                deleteProfile(code.trim(), request.getTargetTenantId());
            }
        }
    }

    @Override
    public List<InfraFileCatalogVO> listCatalogs(String targetTenantId) {
        String tid = resolveTargetTenantId(targetTenantId);
        List<InfraFileCatalogEntity> catalogs = TenantScopeRunner.runIgnoringTenant(() -> {
            List<InfraFileCatalogEntity> tenantRows = catalogMapper.selectByTenant(tid);
            if (!tenantRows.isEmpty() || TenantConstants.PLATFORM_TENANT_ID.equals(tid)) {
                return tenantRows.isEmpty()
                        ? catalogMapper.selectByTenant(TenantConstants.PLATFORM_TENANT_ID)
                        : tenantRows;
            }
            return catalogMapper.selectByTenant(TenantConstants.PLATFORM_TENANT_ID);
        });
        List<InfraFileCatalogVO> out = new ArrayList<>();
        for (InfraFileCatalogEntity catalog : catalogs) {
            out.add(buildCatalogVo(catalog, tid));
        }
        return out;
    }

    @Override
    public InfraFileCatalogVO getCatalog(String catalogCode, String targetTenantId) {
        if (!StringUtils.hasText(catalogCode)) {
            throw new BusinessException("error.validation.required", "fields.catalogCode");
        }
        String tid = resolveTargetTenantId(targetTenantId);
        String code = catalogCode.trim();
        InfraFileCatalogEntity catalog = TenantScopeRunner.runIgnoringTenant(() -> {
            InfraFileCatalogEntity c = catalogMapper.selectByTenantAndCode(tid, code);
            if (c == null) {
                c = catalogMapper.selectByTenantAndCode(TenantConstants.PLATFORM_TENANT_ID, code);
            }
            return c;
        });
        if (catalog == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.fileCatalog");
        }
        return buildCatalogVo(catalog, tid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertCatalog(InfraFileCatalogUpsertRequest request) {
        String tid = resolveTargetTenantId(request.getTargetTenantId());
        requirePlatformOperatorForStructure(tid);
        String code = request.getCatalogCode().trim();
        TenantScopeRunner.runIgnoringTenant(() -> {
            LocalDateTime now = LocalDateTime.now();
            String operator = AuditFieldHelper.operatorId();
            InfraFileCatalogEntity existing = catalogMapper.selectByTenantAndCode(tid, code);
            if (existing == null) {
                InfraFileCatalogEntity entity = new InfraFileCatalogEntity();
                entity.setId(IdGeneratorUtils.nextIdStr());
                entity.setTenantId(tid);
                entity.setCatalogCode(code);
                entity.setCatalogName(request.getCatalogName().trim());
                entity.setCatalogNameI18n(NameI18nJsonHelper.toJson(request.getCatalogNameI18n()));
                entity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "0");
                entity.setRemark(blankToNull(request.getRemark()));
                entity.setCreatedBy(operator);
                entity.setCreatedAt(now);
                entity.setUpdatedBy(operator);
                entity.setUpdatedAt(now);
                entity.setDeleted(0);
                catalogMapper.insert(entity);
            } else {
                existing.setCatalogName(request.getCatalogName().trim());
                if (request.getCatalogNameI18n() != null) {
                    existing.setCatalogNameI18n(NameI18nJsonHelper.toJson(request.getCatalogNameI18n()));
                }
                if (StringUtils.hasText(request.getStatus())) {
                    existing.setStatus(request.getStatus().trim());
                }
                if (request.getRemark() != null) {
                    existing.setRemark(blankToNull(request.getRemark()));
                }
                existing.setUpdatedBy(operator);
                existing.setUpdatedAt(now);
                catalogMapper.updateById(existing);
            }
            if (request.getItems() != null) {
                catalogItemMapper.deleteByTenantAndCatalog(tid, code);
                int sort = 10;
                for (InfraFileCatalogItemUpsertRequest itemReq : request.getItems()) {
                    if (itemReq == null || !StringUtils.hasText(itemReq.getProfileCode())) {
                        continue;
                    }
                    InfraFileCatalogItemEntity item = new InfraFileCatalogItemEntity();
                    item.setId(IdGeneratorUtils.nextIdStr());
                    item.setTenantId(tid);
                    item.setCatalogCode(code);
                    item.setProfileCode(itemReq.getProfileCode().trim());
                    item.setRequired(blankToNull(itemReq.getRequired()));
                    item.setMinNum(itemReq.getMinNum());
                    item.setMaxNum(itemReq.getMaxNum());
                    item.setSortNo(itemReq.getSortNo() == null ? sort : itemReq.getSortNo());
                    item.setStatus(StringUtils.hasText(itemReq.getStatus()) ? itemReq.getStatus().trim() : "0");
                    item.setChromeRoles(ChromeRolesJsonHelper.toJson(itemReq.getChromeRoles()));
                    item.setRemark(blankToNull(itemReq.getRemark()));
                    item.setCreatedBy(operator);
                    item.setCreatedAt(now);
                    item.setUpdatedBy(operator);
                    item.setUpdatedAt(now);
                    item.setDeleted(0);
                    catalogItemMapper.insert(item);
                    sort += 10;
                }
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCatalog(String catalogCode, String targetTenantId) {
        String tid = resolveTargetTenantId(targetTenantId);
        requirePlatformOperatorForStructure(tid);
        if (!StringUtils.hasText(catalogCode)) {
            throw new BusinessException("error.validation.required", "fields.catalogCode");
        }
        String code = catalogCode.trim();
        TenantScopeRunner.runIgnoringTenant(() -> {
            InfraFileCatalogEntity catalog = catalogMapper.selectByTenantAndCode(tid, code);
            if (catalog == null) {
                throw new BusinessException("error.entity.not_found", "entity.name.fileCatalog");
            }
            catalogMapper.logicalDeleteById(catalog.getId());
            catalogItemMapper.deleteByTenantAndCatalog(tid, code);
        });
    }

    private InfraFileCatalogVO buildCatalogVo(InfraFileCatalogEntity catalog, String tid) {
        InfraFileCatalogVO vo = new InfraFileCatalogVO();
        vo.setCatalogCode(catalog.getCatalogCode());
        vo.setCatalogName(catalog.getCatalogName());
        vo.setCatalogNameI18n(NameI18nJsonHelper.parse(catalog.getCatalogNameI18n()));
        vo.setStatus(catalog.getStatus());
        vo.setRemark(catalog.getRemark());
        vo.setTenantOverride(!TenantConstants.PLATFORM_TENANT_ID.equals(catalog.getTenantId()));

        String itemTenant = catalog.getTenantId();
        List<InfraFileCatalogItemEntity> items = TenantScopeRunner.runIgnoringTenant(() -> {
            List<InfraFileCatalogItemEntity> rows =
                    catalogItemMapper.selectByTenantAndCatalog(itemTenant, catalog.getCatalogCode());
            if (rows.isEmpty() && !TenantConstants.PLATFORM_TENANT_ID.equals(itemTenant)) {
                return catalogItemMapper.selectByTenantAndCatalog(
                        TenantConstants.PLATFORM_TENANT_ID, catalog.getCatalogCode());
            }
            if (rows.isEmpty()) {
                return catalogItemMapper.selectByTenantAndCatalog(
                        TenantConstants.PLATFORM_TENANT_ID, catalog.getCatalogCode());
            }
            return rows;
        });

        List<InfraFileCatalogItemVO> itemVos = new ArrayList<>();
        for (InfraFileCatalogItemEntity item : items) {
            InfraFileCatalogItemVO itemVo = new InfraFileCatalogItemVO();
            itemVo.setProfileCode(item.getProfileCode());
            itemVo.setRequired(item.getRequired());
            itemVo.setMinNum(item.getMinNum());
            itemVo.setMaxNum(item.getMaxNum());
            itemVo.setSortNo(item.getSortNo());
            itemVo.setStatus(item.getStatus());
            itemVo.setChromeRoles(ChromeRolesJsonHelper.parse(item.getChromeRoles()));
            Optional<ResolvedFileUploadProfile> resolved =
                    profileResolver.resolve(tid, item.getProfileCode());
            if (resolved.isPresent()) {
                InfraFileProfileVO profileVo = toVoFromResolved(resolved.get());
                InfraFileProfileEntity profileEntity = TenantScopeRunner.runIgnoringTenant(() -> {
                    InfraFileProfileEntity p =
                            profileMapper.selectByTenantAndCode(tid, item.getProfileCode());
                    if (p == null) {
                        p = profileMapper.selectByTenantAndCode(
                                TenantConstants.PLATFORM_TENANT_ID, item.getProfileCode());
                    }
                    return p;
                });
                if (profileEntity != null) {
                    profileVo.setProfileNameI18n(
                            NameI18nJsonHelper.parse(profileEntity.getProfileNameI18n()));
                    if (!StringUtils.hasText(profileVo.getRemark())) {
                        profileVo.setRemark(profileEntity.getRemark());
                    }
                }
                // 场景级覆盖数量/必传
                if (StringUtils.hasText(item.getRequired())) {
                    profileVo.setRequired(YesNoEnum.isYes(item.getRequired()));
                }
                if (item.getMinNum() != null) {
                    profileVo.setMinNum(item.getMinNum());
                }
                if (item.getMaxNum() != null) {
                    profileVo.setMaxNum(item.getMaxNum());
                }
                itemVo.setProfileName(profileVo.getProfileName());
                itemVo.setResolvedProfile(profileVo);
            }
            itemVos.add(itemVo);
        }
        vo.setItems(itemVos);
        return vo;
    }

    private void upsertPlatformProfileAndRule(InfraFileProfileUpsertRequest request, String code) {
        TenantScopeRunner.runIgnoringTenant(() -> {
            InfraFileProfileEntity existing = profileMapper.selectByTenantAndCode(
                    TenantConstants.PLATFORM_TENANT_ID, code);
            LocalDateTime now = LocalDateTime.now();
            String operator = AuditFieldHelper.operatorId();
            if (existing == null) {
                if (!StringUtils.hasText(request.getProfileName())) {
                    throw new BusinessException("error.validation.required", "fields.profileName");
                }
                InfraFileProfileEntity entity = new InfraFileProfileEntity();
                entity.setId(IdGeneratorUtils.nextIdStr());
                entity.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
                entity.setProfileCode(code);
                entity.setProfileName(request.getProfileName().trim());
                entity.setProfileNameI18n(NameI18nJsonHelper.toJson(request.getProfileNameI18n()));
                entity.setCategoryTag(blankToNull(request.getCategoryTag()));
                entity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "0");
                entity.setRemark(blankToNull(request.getRemark()));
                entity.setCreatedBy(operator);
                entity.setCreatedAt(now);
                entity.setUpdatedBy(operator);
                entity.setUpdatedAt(now);
                entity.setDeleted(0);
                profileMapper.insert(entity);
            } else {
                if (StringUtils.hasText(request.getProfileName())) {
                    existing.setProfileName(request.getProfileName().trim());
                }
                if (request.getProfileNameI18n() != null) {
                    existing.setProfileNameI18n(NameI18nJsonHelper.toJson(request.getProfileNameI18n()));
                }
                if (request.getCategoryTag() != null) {
                    existing.setCategoryTag(blankToNull(request.getCategoryTag()));
                }
                if (StringUtils.hasText(request.getStatus())) {
                    existing.setStatus(request.getStatus().trim());
                }
                if (request.getRemark() != null) {
                    existing.setRemark(blankToNull(request.getRemark()));
                }
                existing.setUpdatedBy(operator);
                existing.setUpdatedAt(now);
                profileMapper.updateById(existing);
            }
            upsertRuleRow(TenantConstants.PLATFORM_TENANT_ID, code, request);
        });
    }

    private void upsertTenantRule(String tenantId, String code, InfraFileProfileUpsertRequest request) {
        TenantScopeRunner.runIgnoringTenant(() -> upsertRuleRow(tenantId, code, request));
    }

    private void upsertRuleRow(String tenantId, String code, InfraFileProfileUpsertRequest request) {
        InfraFileProfileRuleEntity existing = ruleMapper.selectByTenantAndCode(tenantId, code);
        LocalDateTime now = LocalDateTime.now();
        String operator = AuditFieldHelper.operatorId();
        if (existing == null) {
            InfraFileProfileRuleEntity entity = new InfraFileProfileRuleEntity();
            entity.setId(IdGeneratorUtils.nextIdStr());
            entity.setTenantId(tenantId);
            entity.setProfileCode(code);
            applyRule(entity, request);
            entity.setCreatedBy(operator);
            entity.setCreatedAt(now);
            entity.setUpdatedBy(operator);
            entity.setUpdatedAt(now);
            entity.setDeleted(0);
            ruleMapper.insert(entity);
        } else {
            applyRule(existing, request);
            existing.setUpdatedBy(operator);
            existing.setUpdatedAt(now);
            ruleMapper.updateById(existing);
        }
    }

    private void applyRule(InfraFileProfileRuleEntity entity, InfraFileProfileUpsertRequest request) {
        entity.setAllowedExts(blankToNull(request.getAllowedExts()));
        entity.setAllowedContentTypes(blankToNull(request.getAllowedContentTypes()));
        entity.setRequired(StringUtils.hasText(request.getRequired()) ? request.getRequired().trim() : "0");
        entity.setMinNum(request.getMinNum() == null ? 0 : request.getMinNum());
        entity.setMaxNum(request.getMaxNum() == null ? 1 : request.getMaxNum());
        entity.setMinSizeBytes(request.getMinSizeBytes() == null ? 0L : request.getMinSizeBytes());
        entity.setMaxSizeBytes(request.getMaxSizeBytes() == null ? 0L : request.getMaxSizeBytes());
        entity.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        entity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "0");
        entity.setRemark(blankToNull(request.getRemark()));
    }

    private void assertWithinPlatformBounds(InfraFileProfileRuleEntity platformRule,
                                            InfraFileProfileUpsertRequest request) {
        if (platformRule == null) {
            return;
        }
        List<String> platformExts = FileUploadProfileResolver.parseCsvLower(platformRule.getAllowedExts());
        List<String> reqExts = FileUploadProfileResolver.parseCsvLower(request.getAllowedExts());
        if (!platformExts.isEmpty() && !reqExts.isEmpty()) {
            for (String ext : reqExts) {
                if (!platformExts.contains(ext)) {
                    throw new BusinessException("error.file.profile_bound_exceeded");
                }
            }
        }
        long platformMax = platformRule.getMaxSizeBytes() == null ? 0L : platformRule.getMaxSizeBytes();
        long reqMax = request.getMaxSizeBytes() == null ? 0L : request.getMaxSizeBytes();
        if (platformMax > 0 && reqMax > platformMax) {
            throw new BusinessException("error.file.profile_bound_exceeded");
        }
        int platformMaxNum = platformRule.getMaxNum() == null ? Integer.MAX_VALUE : platformRule.getMaxNum();
        int reqMaxNum = request.getMaxNum() == null ? 1 : request.getMaxNum();
        if (reqMaxNum > platformMaxNum) {
            throw new BusinessException("error.file.profile_bound_exceeded");
        }
        if (YesNoEnum.isYes(platformRule.getRequired()) && YesNoEnum.isNo(request.getRequired())) {
            throw new BusinessException("error.file.profile_bound_exceeded");
        }
    }

    private InfraFileProfileVO toVo(InfraFileProfileEntity profile, String tid) {
        Optional<ResolvedFileUploadProfile> resolved =
                profileResolver.resolve(tid, profile.getProfileCode());
        if (resolved.isPresent()) {
            InfraFileProfileVO vo = toVoFromResolved(resolved.get());
            vo.setProfileNameI18n(NameI18nJsonHelper.parse(profile.getProfileNameI18n()));
            vo.setRemark(profile.getRemark());
            if (!StringUtils.hasText(vo.getStatus())) {
                vo.setStatus(profile.getStatus());
            }
            return vo;
        }
        InfraFileProfileVO vo = new InfraFileProfileVO();
        vo.setProfileCode(profile.getProfileCode());
        vo.setProfileName(profile.getProfileName());
        vo.setProfileNameI18n(NameI18nJsonHelper.parse(profile.getProfileNameI18n()));
        vo.setCategoryTag(profile.getCategoryTag());
        vo.setStatus(profile.getStatus());
        vo.setRemark(profile.getRemark());
        vo.setRuleTenantId(TenantConstants.PLATFORM_TENANT_ID);
        vo.setTenantOverride(false);
        return vo;
    }

    private static InfraFileProfileVO toVoFromResolved(ResolvedFileUploadProfile r) {
        InfraFileProfileVO vo = new InfraFileProfileVO();
        vo.setProfileCode(r.profileCode());
        vo.setProfileName(r.profileName());
        vo.setCategoryTag(r.categoryTag());
        vo.setRuleTenantId(r.sourceTenantId());
        vo.setTenantOverride(r.tenantOverride());
        vo.setAllowedExts(r.allowedExts());
        vo.setAllowedContentTypes(r.allowedContentTypes());
        vo.setRequired(r.required());
        vo.setMinNum(r.minNum());
        vo.setMaxNum(r.maxNum());
        vo.setMinSizeBytes(r.minSizeBytes());
        vo.setMaxSizeBytes(r.maxSizeBytes());
        vo.setSortNo(r.sortNo());
        vo.setStatus(r.status());
        return vo;
    }

    private String resolveTargetTenantId(String requested) {
        String current = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(current)) {
            throw new BusinessException("error.auth.unauthorized");
        }
        if (!isPlatformOperator()) {
            if (StringUtils.hasText(requested) && !current.equals(requested.trim())) {
                throw new BusinessException("error.forbidden");
            }
            return current;
        }
        if (!StringUtils.hasText(requested)) {
            return TenantConstants.PLATFORM_TENANT_ID;
        }
        return requested.trim();
    }

    /** 结构变更（新增/删除槽位与目录）仅平台运营可做，且目标必须是平台基线 */
    private void requirePlatformOperatorForStructure(String targetTenantId) {
        if (!isPlatformOperator()) {
            throw new BusinessException("error.forbidden");
        }
        if (!TenantConstants.PLATFORM_TENANT_ID.equals(targetTenantId)) {
            throw new BusinessException("error.file.profile_structure_platform_only");
        }
    }

    private static boolean isPlatformOperator() {
        return TenantConstants.PLATFORM_TENANT_ID.equals(SecurityContextHelper.getTenantId());
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
