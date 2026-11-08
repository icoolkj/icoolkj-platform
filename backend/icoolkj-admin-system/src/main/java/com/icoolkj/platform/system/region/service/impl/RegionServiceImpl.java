package com.icoolkj.platform.system.region.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.constant.TenantType;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.region.convert.RegionConvert;
import com.icoolkj.platform.system.region.domain.RegionEntity;
import com.icoolkj.platform.system.tenant.domain.TenantEntity;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.system.region.dto.RegionCreateRequest;
import com.icoolkj.platform.system.region.dto.RegionPageQuery;
import com.icoolkj.platform.system.region.dto.RegionUpdateRequest;
import com.icoolkj.platform.system.region.mapper.RegionMapper;
import com.icoolkj.platform.system.tenant.mapper.TenantMapper;
import com.icoolkj.platform.system.region.service.RegionService;
import com.icoolkj.platform.system.region.vo.RegionVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class RegionServiceImpl implements RegionService {

    private static final Set<String> ENTERPRISE_ROOT_LEVELS = Set.of("1", "2", "3");
    private static final Set<String> ORGANIZATION_ROOT_LEVELS = Set.of("0", "1", "2", "3");
    private static final int MAX_REGION_LEVEL = 5;

    private final RegionMapper regionMapper;
    private final TenantMapper tenantMapper;

    public RegionServiceImpl(RegionMapper regionMapper, TenantMapper tenantMapper) {
        this.regionMapper = regionMapper;
        this.tenantMapper = tenantMapper;
    }

    @Override
    public PageResult<RegionVO> page(RegionPageQuery query) {
        return TenantScopeRunner.runIgnoringTenant(() -> {
            long total = regionMapper.countByQuery(query);
            if (total == 0) {
                return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
            }
            List<RegionEntity> list = regionMapper.selectPage(query);
            if (CollectionUtils.isEmpty(list)) {
                return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
            }
            return PageResult.of(list.stream().map(RegionConvert::toVo).toList(), total, query.getPageNo(), query.getPageSize());
        });
    }

    @Override
    public List<RegionVO> listTree() {
        return TenantScopeRunner.runIgnoringTenant(this::loadAllVo);
    }

    @Override
    public List<RegionVO> listTenantTree() {
        String rootRegionId = resolveCurrentTenantRootRegionId();
        if (!StringUtils.hasText(rootRegionId)) {
            return List.of();
        }
        return TenantScopeRunner.runIgnoringTenant(() -> loadSubtreeVo(rootRegionId));
    }

    @Override
    public List<RegionVO> listSelectableRoots(String tenantType) {
        return TenantScopeRunner.runIgnoringTenant(() ->
                regionMapper.selectSelectableRoots(resolveRootLevels(tenantType)).stream().map(RegionConvert::toVo).toList());
    }

    @Override
    public RegionVO getById(String id) {
        RegionEntity entity = TenantScopeRunner.runIgnoringTenant(() -> regionMapper.selectById(id));
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.region");
        }
        return RegionConvert.toVo(entity);
    }

    @Override
    public void assertRegionSelectableForTenantRoot(String regionId, String tenantType) {
        RegionEntity region = requireRegion(regionId);
        if (!resolveRootLevels(tenantType).contains(region.getRegionLevel())) {
            throw new BusinessException("error.region.root_level_invalid");
        }
    }

    @Override
    public void assertRegionWithinTenantScope(String regionId, String rootRegionId) {
        if (!StringUtils.hasText(regionId) || !StringUtils.hasText(rootRegionId)) {
            throw new BusinessException("error.region.out_of_tenant_scope");
        }
        if (regionId.equals(rootRegionId)) {
            return;
        }
        RegionEntity region = requireRegion(regionId);
        if (containsAncestorId(region.getAncestors(), rootRegionId)) {
            return;
        }
        throw new BusinessException("error.region.out_of_tenant_scope");
    }

    @Override
    public String create(RegionCreateRequest request) {
        return TenantScopeRunner.runIgnoringTenant(() -> {
            String parentId = normalizeParentId(request.getParentId());
            RegionEntity parent = resolveParent(parentId);
            validateRegionLevel(parent, parentId, request.getRegionLevel());
            assertRegionCodeUnique(request.getRegionCode(), null);

            RegionEntity entity = RegionConvert.toEntity(request);
            entity.setId(IdGeneratorUtils.nextIdStr());
            entity.setParentId(parentId);
            entity.setAncestors(buildAncestors(parentId, parent));
            regionMapper.insert(entity);
            return entity.getId();
        });
    }

    @Override
    public void update(RegionUpdateRequest request) {
        TenantScopeRunner.runIgnoringTenant(() -> {
            RegionEntity entity = requireRegion(request.getId());
            String parentId = normalizeParentId(request.getParentId());
            assertParentAllowed(entity.getId(), parentId);
            RegionEntity parent = resolveParent(parentId);
            validateRegionLevel(parent, parentId, request.getRegionLevel());
            assertRegionCodeUnique(request.getRegionCode(), entity.getId());

            String oldAncestors = entity.getAncestors();
            RegionConvert.copy(request, entity);
            entity.setParentId(parentId);
            String newAncestors = buildAncestors(parentId, parent);
            entity.setAncestors(newAncestors);
            regionMapper.updateById(entity);

            if (!newAncestors.equals(oldAncestors)) {
                refreshChildAncestors(entity.getId(), newAncestors);
            }
        });
    }

    @Override
    public void deleteById(String id) {
        TenantScopeRunner.runIgnoringTenant(() -> {
            RegionEntity entity = requireRegion(id);
            List<RegionEntity> children = regionMapper.selectChildren(id);
            if (!CollectionUtils.isEmpty(children)) {
                throw new BusinessException("error.region.has_child");
            }
            if (regionMapper.countTenantsByRootInSubtree(id) > 0) {
                throw new BusinessException("error.region.in_use_by_tenant");
            }
            AuditFieldHelper.markDeleted(entity);
            regionMapper.updateById(entity);
        });
    }

    private List<String> resolveRootLevels(String tenantType) {
        if (TenantType.isOrganization(tenantType)) {
            return ORGANIZATION_ROOT_LEVELS.stream().toList();
        }
        return ENTERPRISE_ROOT_LEVELS.stream().toList();
    }

    private List<RegionVO> loadAllVo() {
        return regionMapper.selectAllActive().stream().map(RegionConvert::toVo).toList();
    }

    private List<RegionVO> loadSubtreeVo(String rootRegionId) {
        return regionMapper.selectSubtree(rootRegionId).stream().map(RegionConvert::toVo).toList();
    }

    private String resolveCurrentTenantRootRegionId() {
        String tenantId = TenantContextHolder.getTenantId();
        if (!StringUtils.hasText(tenantId) || TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)) {
            return null;
        }
        TenantEntity tenant = tenantMapper.selectById(tenantId);
        return tenant == null ? null : tenant.getRootRegionId();
    }

    private RegionEntity requireRegion(String regionId) {
        RegionEntity entity = TenantScopeRunner.runIgnoringTenant(() -> regionMapper.selectById(regionId));
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.region");
        }
        return entity;
    }

    private boolean containsAncestorId(String ancestors, String regionId) {
        if (!StringUtils.hasText(ancestors) || !StringUtils.hasText(regionId)) {
            return false;
        }
        return Arrays.asList(ancestors.split(",")).contains(regionId);
    }

    private String normalizeParentId(String parentId) {
        return StringUtils.hasText(parentId) ? parentId.trim() : CommonConstants.TREE_ROOT_PARENT_ID;
    }

    private RegionEntity resolveParent(String parentId) {
        if (CommonConstants.TREE_ROOT_PARENT_ID.equals(parentId)) {
            return null;
        }
        RegionEntity parent = regionMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.region");
        }
        return parent;
    }

    private String buildAncestors(String parentId, RegionEntity parent) {
        if (CommonConstants.TREE_ROOT_PARENT_ID.equals(parentId)) {
            return CommonConstants.TREE_ROOT_PARENT_ID;
        }
        if (parent == null) {
            return CommonConstants.TREE_ROOT_PARENT_ID;
        }
        String parentAncestors = StringUtils.hasText(parent.getAncestors()) ? parent.getAncestors() : CommonConstants.TREE_ROOT_PARENT_ID;
        return parentAncestors + "," + parent.getId();
    }

    private void validateRegionLevel(RegionEntity parent, String parentId, String regionLevel) {
        int level;
        try {
            level = Integer.parseInt(regionLevel);
        } catch (NumberFormatException ex) {
            throw new BusinessException("error.region.level_invalid");
        }
        if (level < 0 || level > MAX_REGION_LEVEL) {
            throw new BusinessException("error.region.level_invalid");
        }
        if (CommonConstants.TREE_ROOT_PARENT_ID.equals(parentId)) {
            if (level != 0) {
                throw new BusinessException("error.region.level_invalid");
            }
            return;
        }
        if (parent == null) {
            throw new BusinessException("error.region.level_invalid");
        }
        int expected = Integer.parseInt(parent.getRegionLevel()) + 1;
        if (level != expected) {
            throw new BusinessException("error.region.level_invalid");
        }
    }

    private void assertRegionCodeUnique(String regionCode, String excludeId) {
        RegionEntity existing = regionMapper.selectByRegionCode(regionCode);
        if (existing != null && (excludeId == null || !excludeId.equals(existing.getId()))) {
            throw new BusinessException("error.region.code_duplicate");
        }
    }

    private void assertParentAllowed(String regionId, String parentId) {
        if (regionId.equals(parentId)) {
            throw new BusinessException("error.region.parent_self");
        }
        if (CommonConstants.TREE_ROOT_PARENT_ID.equals(parentId)) {
            return;
        }
        RegionEntity parent = regionMapper.selectById(parentId);
        if (parent == null) {
            return;
        }
        if (containsAncestorId(parent.getAncestors(), regionId) || regionId.equals(parent.getId())) {
            throw new BusinessException("error.region.parent_child");
        }
    }

    private void refreshChildAncestors(String parentId, String parentAncestors) {
        List<RegionEntity> children = regionMapper.selectChildren(parentId);
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        for (RegionEntity child : children) {
            String childAncestors = parentAncestors + "," + parentId;
            child.setAncestors(childAncestors);
            regionMapper.updateById(child);
            refreshChildAncestors(child.getId(), childAncestors);
        }
    }
}
