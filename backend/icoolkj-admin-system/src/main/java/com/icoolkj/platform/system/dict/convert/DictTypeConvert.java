package com.icoolkj.platform.system.dict.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.dict.domain.DictTypeEntity;
import com.icoolkj.platform.system.dict.dto.DictTypeCreateRequest;
import com.icoolkj.platform.system.dict.dto.DictTypeUpdateRequest;
import com.icoolkj.platform.system.dict.vo.DictTypeVO;

/** 字典类型表对象转换 */
public final class DictTypeConvert {

    private DictTypeConvert() {
    }

    public static DictTypeVO toVo(DictTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        DictTypeVO vo = new DictTypeVO();
        vo.setId(entity.getId());
        vo.setDictName(entity.getDictName());
        vo.setDictType(entity.getDictType());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    public static DictTypeEntity toEntity(DictTypeCreateRequest request) {
        DictTypeEntity entity = new DictTypeEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setDictName(request.getDictName());
        entity.setDictType(request.getDictType());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(DictTypeUpdateRequest request, DictTypeEntity entity) {
        entity.setDictName(request.getDictName());
        entity.setDictType(request.getDictType());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
    }
}
