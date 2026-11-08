package com.icoolkj.platform.system.dept.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import com.icoolkj.platform.framework.datascope.RowAccessSupport;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.system.dept.convert.DeptConvert;
import com.icoolkj.platform.system.dept.domain.DeptEntity;
import com.icoolkj.platform.system.dept.dto.DeptCreateRequest;
import com.icoolkj.platform.system.dept.dto.DeptPageQuery;
import com.icoolkj.platform.system.dept.dto.DeptUpdateRequest;
import com.icoolkj.platform.system.dept.mapper.DeptMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.dept.service.DeptService;
import com.icoolkj.platform.system.dept.vo.DeptVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 部门表服务实现 */
@Service
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;
    private final UserMapper userMapper;
    private final RowAccessSupport rowAccessSupport;

    public DeptServiceImpl(DeptMapper deptMapper, UserMapper userMapper, RowAccessSupport rowAccessSupport) {
        this.deptMapper = deptMapper;
        this.userMapper = userMapper;
        this.rowAccessSupport = rowAccessSupport;
    }

    @Override
    public PageResult<DeptVO> page(DeptPageQuery query) {
        return TenantQueryScope.runForExplicitTenant(query, () -> {
            long total = deptMapper.countByQuery(query);
            if (total == 0) {
                return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
            }
            List<DeptEntity> list = deptMapper.selectPage(query);
            if (CollectionUtils.isEmpty(list)) {
                return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
            }
            return PageResult.of(list.stream().map(DeptConvert::toVo).toList(), total, query.getPageNo(), query.getPageSize());
        });
    }

    @Override
    public DeptVO getById(String id) {
        DeptEntity entity = requireDept(id);
        assertRowAccess(entity);
        return DeptConvert.toVo(entity);
    }

    @Override
    public String create(DeptCreateRequest request) {
        String tenantId = requireTenantId();
        String parentId = normalizeParentId(request.getParentId());
        DeptEntity parent = resolveParent(tenantId, parentId);
        assertDeptCodeAvailable(tenantId, request.getDeptCode(), null);

        DeptEntity entity = DeptConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        entity.setTenantId(tenantId);
        entity.setParentId(parentId);
        entity.setAncestors(buildAncestors(parentId, parent));
        deptMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(DeptUpdateRequest request) {
        String tenantId = requireTenantId();
        DeptEntity entity = requireDept(request.getId());
        assertSameTenant(entity, tenantId);
        assertRowAccess(entity);
        assertDeptCodeAvailable(tenantId, request.getDeptCode(), entity.getId());

        String parentId = normalizeParentId(request.getParentId());
        assertParentAllowed(entity.getId(), parentId);
        DeptEntity parent = resolveParent(tenantId, parentId);

        String oldAncestors = entity.getAncestors();
        DeptConvert.copy(request, entity);
        entity.setParentId(parentId);
        String newAncestors = buildAncestors(parentId, parent);
        entity.setAncestors(newAncestors);
        deptMapper.updateById(entity);

        if (!newAncestors.equals(oldAncestors)) {
            refreshChildAncestors(tenantId, entity.getId(), newAncestors);
        }
    }

    @Override
    public void deleteById(String id) {
        String tenantId = requireTenantId();
        DeptEntity entity = requireDept(id);
        assertSameTenant(entity, tenantId);
        assertRowAccess(entity);

        List<DeptEntity> children = deptMapper.selectChildren(tenantId, id);
        if (!CollectionUtils.isEmpty(children)) {
            throw new BusinessException("error.dept.has_child");
        }
        if (userMapper.countByDeptId(id) > 0) {
            throw new BusinessException("error.dept.has_user");
        }
        AuditFieldHelper.markDeleted(entity);
        deptMapper.updateById(entity);
    }

    private DeptEntity requireDept(String id) {
        DeptEntity entity = deptMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.dept");
        }
        return entity;
    }

    private void assertDeptCodeAvailable(String tenantId, String deptCode, String excludeId) {
        if (!StringUtils.hasText(deptCode)) {
            return;
        }
        DeptEntity query = new DeptEntity();
        query.setTenantId(tenantId);
        query.setDeptCode(deptCode.trim());
        List<DeptEntity> list = deptMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        boolean duplicate = list.stream().anyMatch(item -> excludeId == null || !excludeId.equals(item.getId()));
        if (duplicate) {
            throw new BusinessException("error.dept.code_exists");
        }
    }

    private String requireTenantId() {
        String tenantId = TenantContextHolder.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException("error.tenant.unresolved");
        }
        return tenantId;
    }

    private void assertSameTenant(DeptEntity entity, String tenantId) {
        if (!tenantId.equals(entity.getTenantId())) {
            throw new BusinessException("error.entity.not_found", "entity.name.dept");
        }
    }

    private void assertRowAccess(DeptEntity entity) {
        rowAccessSupport.assertReadable(
                entity.getTenantId(), DataScopeSpec.deptAsId(), null, entity.getId());
    }

    private String normalizeParentId(String parentId) {
        return StringUtils.hasText(parentId) ? parentId.trim() : CommonConstants.TREE_ROOT_PARENT_ID;
    }

    private DeptEntity resolveParent(String tenantId, String parentId) {
        if (CommonConstants.TREE_ROOT_PARENT_ID.equals(parentId)) {
            return null;
        }
        DeptEntity parent = deptMapper.selectById(parentId);
        if (parent == null || !tenantId.equals(parent.getTenantId())) {
            throw new BusinessException("error.entity.not_found", "entity.name.dept");
        }
        return parent;
    }

    private String buildAncestors(String parentId, DeptEntity parent) {
        if (CommonConstants.TREE_ROOT_PARENT_ID.equals(parentId)) {
            return CommonConstants.TREE_ROOT_PARENT_ID;
        }
        if (parent == null) {
            return CommonConstants.TREE_ROOT_PARENT_ID;
        }
        String parentAncestors = StringUtils.hasText(parent.getAncestors()) ? parent.getAncestors() : CommonConstants.TREE_ROOT_PARENT_ID;
        return parentAncestors + "," + parent.getId();
    }

    private void assertParentAllowed(String deptId, String parentId) {
        if (deptId.equals(parentId)) {
            throw new BusinessException("error.dept.parent_self");
        }
        if (CommonConstants.TREE_ROOT_PARENT_ID.equals(parentId)) {
            return;
        }
        DeptEntity parent = deptMapper.selectById(parentId);
        if (parent == null) {
            return;
        }
        if (containsAncestorId(parent.getAncestors(), deptId) || deptId.equals(parent.getId())) {
            throw new BusinessException("error.dept.parent_child");
        }
    }

    private boolean containsAncestorId(String ancestors, String deptId) {
        if (!StringUtils.hasText(ancestors) || !StringUtils.hasText(deptId)) {
            return false;
        }
        return Arrays.asList(ancestors.split(",")).contains(deptId);
    }

    private void refreshChildAncestors(String tenantId, String parentId, String parentAncestors) {
        List<DeptEntity> children = deptMapper.selectChildren(tenantId, parentId);
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        for (DeptEntity child : children) {
            String childAncestors = parentAncestors + "," + parentId;
            child.setAncestors(childAncestors);
            deptMapper.updateById(child);
            refreshChildAncestors(tenantId, child.getId(), childAncestors);
        }
    }
}
