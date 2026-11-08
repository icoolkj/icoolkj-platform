package com.icoolkj.platform.system.tenant.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.SodRoleCodes;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.role.domain.RoleMenuEntity;
import com.icoolkj.platform.system.role.mapper.RoleMapper;
import com.icoolkj.platform.system.role.mapper.RoleMenuMapper;
import com.icoolkj.platform.system.tenant.service.TenantRoleBootstrapService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TenantRoleBootstrapServiceImpl implements TenantRoleBootstrapService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;

    public TenantRoleBootstrapServiceImpl(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public void bootstrapSodRoles(String tenantId) {
        if (!StringUtils.hasText(tenantId) || TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)) {
            return;
        }
        TenantScopeRunner.runIgnoringTenant(() -> {
            for (String roleCode : SodRoleCodes.ALL) {
                if (existsRole(tenantId, roleCode)) {
                    continue;
                }
                RoleEntity template = findTemplate(roleCode);
                if (template == null) {
                    continue;
                }
                RoleEntity role = new RoleEntity();
                role.setId(IdGeneratorUtils.nextIdStr());
                role.setTenantId(tenantId);
                role.setRoleName(template.getRoleName());
                role.setRoleCode(template.getRoleCode());
                role.setDataScope(template.getDataScope());
                role.setSortNo(template.getSortNo());
                role.setStatus(CommonConstants.STATUS_NORMAL);
                role.setIsSuperRole(CommonConstants.NO);
                role.setRemark(template.getRemark());
                AuditFieldHelper.fillInsert(role);
                roleMapper.insert(role);

                List<String> menuIds = roleMenuMapper.selectMenuIdsByTenantAndRoleIds(
                        template.getTenantId(), List.of(template.getId()));
                if (CollectionUtils.isEmpty(menuIds)) {
                    continue;
                }
                for (String menuId : menuIds) {
                    RoleMenuEntity rm = new RoleMenuEntity();
                    rm.setTenantId(tenantId);
                    rm.setRoleId(role.getId());
                    rm.setMenuId(menuId);
                    roleMenuMapper.insert(rm);
                }
            }
            return null;
        });
    }

    private boolean existsRole(String tenantId, String roleCode) {
        RoleEntity query = new RoleEntity();
        query.setTenantId(tenantId);
        query.setRoleCode(roleCode);
        List<RoleEntity> list = roleMapper.selectList(query);
        return list != null && !list.isEmpty();
    }

    private RoleEntity findTemplate(String roleCode) {
        RoleEntity query = new RoleEntity();
        query.setTenantId(SodRoleCodes.TEMPLATE_TENANT_ID);
        query.setRoleCode(roleCode);
        List<RoleEntity> list = roleMapper.selectList(query);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
}
