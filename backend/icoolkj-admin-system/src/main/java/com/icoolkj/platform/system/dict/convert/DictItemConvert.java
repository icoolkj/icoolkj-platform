package com.icoolkj.platform.system.dict.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.dict.domain.DictItemEntity;
import com.icoolkj.platform.system.dict.dto.DictItemCreateRequest;
import com.icoolkj.platform.system.dict.dto.DictItemUpdateRequest;
import com.icoolkj.platform.system.dict.vo.DictItemVO;

/** 字典数据项表对象转换 */
public final class DictItemConvert {

    private DictItemConvert() {
    }

    public static DictItemVO toVo(DictItemEntity entity) {
        if (entity == null) {
            return null;
        }
        DictItemVO vo = new DictItemVO();
        vo.setId(entity.getId());
        vo.setDictTypeId(entity.getDictTypeId());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictValue(entity.getDictValue());
        vo.setValueType(entity.getValueType());
        vo.setCssClass(entity.getCssClass());
        vo.setIcon(entity.getIcon());
        vo.setListClass(entity.getListClass());
        vo.setIsDefault(entity.getIsDefault());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    public static DictItemEntity toEntity(DictItemCreateRequest request) {
        DictItemEntity entity = new DictItemEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setDictTypeId(request.getDictTypeId());
        entity.setDictLabel(request.getDictLabel());
        entity.setDictValue(request.getDictValue());
        entity.setValueType(request.getValueType());
        entity.setCssClass(request.getCssClass());
        entity.setIcon(request.getIcon());
        entity.setListClass(request.getListClass());
        entity.setIsDefault(request.getIsDefault());
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(DictItemUpdateRequest request, DictItemEntity entity) {
        entity.setDictTypeId(request.getDictTypeId());
        entity.setDictLabel(request.getDictLabel());
        entity.setDictValue(request.getDictValue());
        entity.setValueType(request.getValueType());
        entity.setCssClass(request.getCssClass());
        entity.setIcon(request.getIcon());
        entity.setListClass(request.getListClass());
        entity.setIsDefault(request.getIsDefault());
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
    }
}
