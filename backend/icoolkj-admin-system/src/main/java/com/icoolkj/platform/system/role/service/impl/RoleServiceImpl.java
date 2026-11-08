package com.icoolkj.platform.system.role.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.security.service.LoginSessionService;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.system.role.convert.RoleConvert;
import com.icoolkj.platform.system.role.domain.RoleDeptEntity;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.role.domain.RoleMenuEntity;
import com.icoolkj.platform.system.user.domain.UserRoleEntity;
import com.icoolkj.platform.system.role.dto.RoleCreateRequest;
import com.icoolkj.platform.system.role.dto.RolePageQuery;
import com.icoolkj.platform.system.role.dto.RoleUpdateRequest;
import com.icoolkj.platform.system.role.mapper.RoleDeptMapper;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import com.icoolkj.platform.system.role.mapper.RoleMenuMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import com.icoolkj.platform.system.role.service.RoleService;
import com.icoolkj.platform.system.role.vo.RoleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 角色表服务实现 */
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final RoleDeptMapper roleDeptMapper;
    private final LoginSessionService loginSessionService;

    public RoleServiceImpl(RoleMapper roleMapper,
                           UserRoleMapper userRoleMapper,
                           RoleMenuMapper roleMenuMapper,
                           RoleDeptMapper roleDeptMapper,
                           LoginSessionService loginSessionService) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.roleDeptMapper = roleDeptMapper;
        this.loginSessionService = loginSessionService;
    }

    @Override
    public PageResult<RoleVO> page(RolePageQuery query) {
        TenantQueryScope.applyPageQuery(query);
        return TenantQueryScope.runForExplicitTenant(query, () -> {
            long total = roleMapper.countByQuery(query);
            if (total == 0) {
                return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
            }
            List<RoleEntity> list = roleMapper.selectPage(query);
            if (CollectionUtils.isEmpty(list)) {
                return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
            }
            return PageResult.of(list.stream().map(RoleConvert::toVo).toList(), total, query.getPageNo(), query.getPageSize());
        });
    }

    @Override
    public RoleVO getById(String id) {
        RoleEntity entity = roleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.role");
        }
        return RoleConvert.toVo(entity);
    }

    @Override
    public String create(RoleCreateRequest request) {
        assertRoleCodeAvailable(request.getRoleCode(), null);
        RoleEntity entity = RoleConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        roleMapper.insert(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    public void update(RoleUpdateRequest request) {
        RoleEntity entity = roleMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.role");
        }
        assertRoleCodeAvailable(request.getRoleCode(), entity.getId());
        RoleConvert.copy(request, entity);
        roleMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        RoleEntity entity = roleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.role");
        }
        String tenantId = entity.getTenantId();
        Set<String> affectedUserIds = collectUserIds(tenantId, id);

        AuditFieldHelper.markDeleted(entity);
        roleMapper.updateById(entity);

        UserRoleEntity userRoleDelete = new UserRoleEntity();
        userRoleDelete.setTenantId(tenantId);
        userRoleDelete.setRoleId(id);
        userRoleMapper.delete(userRoleDelete);

        RoleMenuEntity roleMenuDelete = new RoleMenuEntity();
        roleMenuDelete.setTenantId(tenantId);
        roleMenuDelete.setRoleId(id);
        roleMenuMapper.delete(roleMenuDelete);

        RoleDeptEntity roleDeptDelete = new RoleDeptEntity();
        roleDeptDelete.setTenantId(tenantId);
        roleDeptDelete.setRoleId(id);
        roleDeptMapper.delete(roleDeptDelete);

        for (String userId : affectedUserIds) {
            loginSessionService.removeAllUserSessions(tenantId, userId);
        }
    }

    private Set<String> collectUserIds(String tenantId, String roleId) {
        UserRoleEntity query = new UserRoleEntity();
        query.setTenantId(tenantId);
        query.setRoleId(roleId);
        List<UserRoleEntity> rows = userRoleMapper.selectList(query);
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptySet();
        }
        Set<String> userIds = new HashSet<>();
        for (UserRoleEntity row : rows) {
            if (row != null && StringUtils.hasText(row.getUserId())) {
                userIds.add(row.getUserId());
            }
        }
        return userIds;
    }

    private void assertRoleCodeAvailable(String roleCode, String excludeId) {
        if (!StringUtils.hasText(roleCode)) {
            return;
        }
        RoleEntity query = new RoleEntity();
        query.setRoleCode(roleCode.trim());
        List<RoleEntity> list = roleMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        boolean duplicate = list.stream().anyMatch(item -> excludeId == null || !excludeId.equals(item.getId()));
        if (duplicate) {
            throw new BusinessException("error.role.code_exists");
        }
    }
}
