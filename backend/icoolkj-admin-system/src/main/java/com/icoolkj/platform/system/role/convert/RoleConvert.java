package com.icoolkj.platform.system.role.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.role.domain.RoleEntity;
import com.icoolkj.platform.system.role.dto.RoleCreateRequest;
import com.icoolkj.platform.system.role.dto.RoleUpdateRequest;
import com.icoolkj.platform.system.role.vo.RoleVO;

/** 角色表对象转换 */
public final class RoleConvert {

    private RoleConvert() {
    }

    public static RoleVO toVo(RoleEntity entity) {
        if (entity == null) {
            return null;
        }
        RoleVO vo = new RoleVO();
        vo.setId(entity.getId());
        vo.setRoleName(entity.getRoleName());
        vo.setRoleCode(entity.getRoleCode());
        vo.setDataScope(entity.getDataScope());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setIsSuperRole(entity.getIsSuperRole());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static RoleEntity toEntity(RoleCreateRequest request) {
        RoleEntity entity = new RoleEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setRoleName(request.getRoleName());
        entity.setRoleCode(request.getRoleCode());
        entity.setDataScope(request.getDataScope());
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setIsSuperRole(request.getIsSuperRole());
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(RoleUpdateRequest request, RoleEntity entity) {
        entity.setRoleName(request.getRoleName());
        entity.setRoleCode(request.getRoleCode());
        entity.setDataScope(request.getDataScope());
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setIsSuperRole(request.getIsSuperRole());
        entity.setRemark(request.getRemark());
    }
}
