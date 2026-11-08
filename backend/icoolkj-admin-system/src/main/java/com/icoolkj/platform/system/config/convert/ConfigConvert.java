package com.icoolkj.platform.system.config.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.config.domain.ConfigEntity;
import com.icoolkj.platform.system.config.dto.ConfigCreateRequest;
import com.icoolkj.platform.system.config.dto.ConfigUpdateRequest;
import com.icoolkj.platform.system.config.vo.ConfigVO;

/** 系统参数表对象转换 */
public final class ConfigConvert {

    private ConfigConvert() {
    }

    public static ConfigVO toVo(ConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        ConfigVO vo = new ConfigVO();
        vo.setId(entity.getId());
        vo.setConfigName(entity.getConfigName());
        vo.setConfigKey(entity.getConfigKey());
        vo.setConfigValue(entity.getConfigValue());
        vo.setConfigType(entity.getConfigType());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static ConfigEntity toEntity(ConfigCreateRequest request) {
        ConfigEntity entity = new ConfigEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setConfigName(request.getConfigName());
        entity.setConfigKey(request.getConfigKey());
        entity.setConfigValue(request.getConfigValue());
        entity.setConfigType(request.getConfigType());
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(ConfigUpdateRequest request, ConfigEntity entity) {
        entity.setConfigName(request.getConfigName());
        entity.setConfigKey(request.getConfigKey());
        entity.setConfigValue(request.getConfigValue());
        entity.setConfigType(request.getConfigType());
        entity.setRemark(request.getRemark());
    }
}
