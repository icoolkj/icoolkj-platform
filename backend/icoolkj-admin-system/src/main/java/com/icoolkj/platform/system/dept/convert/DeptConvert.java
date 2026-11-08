package com.icoolkj.platform.system.dept.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.dept.domain.DeptEntity;
import com.icoolkj.platform.system.dept.dto.DeptCreateRequest;
import com.icoolkj.platform.system.dept.dto.DeptUpdateRequest;
import com.icoolkj.platform.system.dept.vo.DeptVO;

/** 部门表对象转换 */
public final class DeptConvert {

    private DeptConvert() {
    }

    public static DeptVO toVo(DeptEntity entity) {
        if (entity == null) {
            return null;
        }
        DeptVO vo = new DeptVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setAncestors(entity.getAncestors());
        vo.setDeptName(entity.getDeptName());
        vo.setDeptCode(entity.getDeptCode());
        vo.setLeaderName(entity.getLeaderName());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static DeptEntity toEntity(DeptCreateRequest request) {
        DeptEntity entity = new DeptEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setParentId(request.getParentId());
        entity.setDeptName(request.getDeptName());
        entity.setDeptCode(request.getDeptCode());
        entity.setLeaderName(request.getLeaderName());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(DeptUpdateRequest request, DeptEntity entity) {
        entity.setParentId(request.getParentId());
        entity.setDeptName(request.getDeptName());
        entity.setDeptCode(request.getDeptCode());
        entity.setLeaderName(request.getLeaderName());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
    }
}
