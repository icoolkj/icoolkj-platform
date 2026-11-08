package com.icoolkj.platform.system.tenant.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantType;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.system.tenant.convert.TenantConvert;
import com.icoolkj.platform.system.tenant.domain.TenantEntity;
import com.icoolkj.platform.system.tenant.dto.TenantBootstrapOptions;
import com.icoolkj.platform.system.tenant.dto.TenantCreateRequest;
import com.icoolkj.platform.system.tenant.dto.TenantPageQuery;
import com.icoolkj.platform.system.tenant.dto.TenantStatusUpdateRequest;
import com.icoolkj.platform.system.tenant.dto.TenantUpdateRequest;
import com.icoolkj.platform.system.tenant.mapper.TenantMapper;
import com.icoolkj.platform.system.region.service.RegionService;
import com.icoolkj.platform.system.tenant.service.TenantBootstrapService;
import com.icoolkj.platform.system.tenant.service.TenantRoleBootstrapService;
import com.icoolkj.platform.system.tenant.service.TenantService;
import com.icoolkj.platform.system.region.vo.RegionVO;
import com.icoolkj.platform.system.tenant.vo.TenantProvisionVO;
import com.icoolkj.platform.system.tenant.vo.TenantVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/** 租户表服务实现 */
@Service
public class TenantServiceImpl implements TenantService {

    private final TenantMapper tenantMapper;
    private final RegionService regionService;
    private final TenantRoleBootstrapService tenantRoleBootstrapService;
    private final TenantBootstrapService tenantBootstrapService;

    public TenantServiceImpl(TenantMapper tenantMapper,
                             RegionService regionService,
                             TenantRoleBootstrapService tenantRoleBootstrapService,
                             TenantBootstrapService tenantBootstrapService) {
        this.tenantMapper = tenantMapper;
        this.regionService = regionService;
        this.tenantRoleBootstrapService = tenantRoleBootstrapService;
        this.tenantBootstrapService = tenantBootstrapService;
    }

    @Override
    public PageResult<TenantVO> page(TenantPageQuery query) {
        long total = tenantMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<TenantEntity> list = tenantMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        return PageResult.of(list.stream().map(this::toVoWithRegion).toList(), total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public TenantVO getById(String id) {
        TenantEntity entity = tenantMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.tenant");
        }
        return toVoWithRegion(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantProvisionVO create(TenantCreateRequest request) {
        validateTenantType(request.getTenantType());
        validateRootRegion(request.getRootRegionId(), request.getTenantType());
        validateParentTenant(null, request.getTenantType(), request.getParentTenantId(), request.getRootRegionId());
        assertTenantCodeAvailable(request.getTenantCode(), null);

        TenantEntity entity = TenantConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        entity.setAncestors(buildTenantAncestors(request.getParentTenantId()));
        tenantMapper.insert(entity);
        tenantRoleBootstrapService.bootstrapSodRoles(entity.getId());
        TenantBootstrapOptions options = new TenantBootstrapOptions();
        options.setAdminUsername(request.getAdminUsername());
        options.setRootDeptCode(request.getRootDeptCode());
        options.setRootDeptName(request.getRootDeptName());
        return tenantBootstrapService.bootstrap(entity, options);
    }

    @Override
    public void update(TenantUpdateRequest request) {
        TenantEntity entity = tenantMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.tenant");
        }
        if (StringUtils.hasText(request.getTenantCode())) {
            assertTenantCodeAvailable(request.getTenantCode(), entity.getId());
        }
        String rootRegionId = StringUtils.hasText(request.getRootRegionId()) ? request.getRootRegionId() : entity.getRootRegionId();
        if (StringUtils.hasText(request.getRootRegionId())) {
            validateRootRegion(rootRegionId, entity.getTenantType());
        }
        String parentTenantId = request.getParentTenantId() != null ? request.getParentTenantId() : entity.getParentTenantId();
        validateParentTenant(entity.getId(), entity.getTenantType(), parentTenantId, rootRegionId);

        TenantConvert.copy(request, entity);
        entity.setRootRegionId(rootRegionId);
        entity.setParentTenantId(parentTenantId);
        entity.setAncestors(buildTenantAncestors(parentTenantId));
        tenantMapper.updateById(entity);
    }

    @Override
    public void updateStatus(TenantStatusUpdateRequest request) {
        TenantEntity entity = tenantMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.tenant");
        }
        entity.setStatus(request.getStatus());
        tenantMapper.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        TenantEntity entity = tenantMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.tenant");
        }
        AuditFieldHelper.markDeleted(entity);
        tenantMapper.updateById(entity);
    }

    private void validateTenantType(String tenantType) {
        if (!TenantType.isEnterprise(tenantType) && !TenantType.isOrganization(tenantType)) {
            throw new BusinessException("error.tenant.type_invalid");
        }
    }

    private void assertTenantCodeAvailable(String tenantCode, String excludeId) {
        if (!StringUtils.hasText(tenantCode)) {
            return;
        }
        TenantEntity existing = tenantMapper.selectByTenantCode(tenantCode.trim());
        if (existing != null && (excludeId == null || !excludeId.equals(existing.getId()))) {
            throw new BusinessException("gen.tenant.tenantCode.exists");
        }
    }

    private void validateRootRegion(String rootRegionId, String tenantType) {
        if (!StringUtils.hasText(rootRegionId)) {
            throw new BusinessException("error.tenant.root_region_required");
        }
        regionService.assertRegionSelectableForTenantRoot(rootRegionId, tenantType);
    }

    private void validateParentTenant(String tenantId, String tenantType, String parentTenantId, String rootRegionId) {
        if (TenantType.isEnterprise(tenantType)) {
            if (StringUtils.hasText(parentTenantId)) {
                throw new BusinessException("error.tenant.enterprise_no_parent");
            }
            return;
        }
        if (!StringUtils.hasText(parentTenantId) || CommonConstants.TREE_ROOT_PARENT_ID.equals(parentTenantId)) {
            return;
        }
        if (tenantId != null && tenantId.equals(parentTenantId)) {
            throw new BusinessException("error.tenant.parent_self");
        }
        TenantEntity parent = tenantMapper.selectById(parentTenantId);
        if (parent == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.tenant");
        }
        if (!TenantType.isOrganization(parent.getTenantType())) {
            throw new BusinessException("error.tenant.parent_type_invalid");
        }
        if (StringUtils.hasText(parent.getRootRegionId())) {
            regionService.assertRegionWithinTenantScope(rootRegionId, parent.getRootRegionId());
        }
    }

    private String buildTenantAncestors(String parentTenantId) {
        if (!StringUtils.hasText(parentTenantId) || CommonConstants.TREE_ROOT_PARENT_ID.equals(parentTenantId)) {
            return CommonConstants.TREE_ROOT_PARENT_ID;
        }
        TenantEntity parent = tenantMapper.selectById(parentTenantId);
        if (parent == null) {
            return CommonConstants.TREE_ROOT_PARENT_ID;
        }
        String parentAncestors = StringUtils.hasText(parent.getAncestors()) ? parent.getAncestors() : CommonConstants.TREE_ROOT_PARENT_ID;
        return parentAncestors + "," + parent.getId();
    }

    private TenantVO toVoWithRegion(TenantEntity entity) {
        TenantVO vo = TenantConvert.toVo(entity);
        if (vo != null && StringUtils.hasText(vo.getRootRegionId())) {
            try {
                RegionVO region = regionService.getById(vo.getRootRegionId());
                vo.setRootRegionName(region.getRegionName());
            } catch (BusinessException ignored) {
                // region may be removed in dev
            }
        }
        return vo;
    }
}
