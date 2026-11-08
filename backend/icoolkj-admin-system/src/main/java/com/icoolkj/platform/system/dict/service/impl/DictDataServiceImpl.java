package com.icoolkj.platform.system.dict.service.impl;

import com.icoolkj.platform.common.constant.DictTypeScope;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.dict.domain.DictItemEntity;
import com.icoolkj.platform.system.dict.domain.DictTypeEntity;
import com.icoolkj.platform.system.dict.mapper.DictItemMapper;
import com.icoolkj.platform.system.dict.mapper.DictTypeMapper;
import com.icoolkj.platform.system.dict.service.DictDataService;
import com.icoolkj.platform.system.dict.vo.DictDataVO;
import com.icoolkj.platform.system.dict.vo.DictTypeOptionVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime dictionary reads merge tenant + platform ({@code tenant_id=0}) rows.
 * Queries run with tenant-line ignored because the plugin would otherwise hide platform dicts.
 */
@Service
public class DictDataServiceImpl implements DictDataService {

    private final DictItemMapper dictItemMapper;
    private final DictTypeMapper dictTypeMapper;

    public DictDataServiceImpl(DictItemMapper dictItemMapper, DictTypeMapper dictTypeMapper) {
        this.dictItemMapper = dictItemMapper;
        this.dictTypeMapper = dictTypeMapper;
    }

    @Override
    public List<DictDataVO> listByDictType(String dictType) {
        if (StringUtils.isBlank(dictType)) {
            return Collections.emptyList();
        }
        String tenantId = resolveTenantId();
        List<DictItemEntity> items = TenantScopeRunner.runIgnoringTenant(
                () -> dictItemMapper.selectActiveByDictType(dictType.trim(), tenantId));
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        return items.stream().map(this::toVo).toList();
    }

    @Override
    public Map<String, List<DictDataVO>> listByDictTypes(List<String> dictTypes) {
        Map<String, List<DictDataVO>> result = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(dictTypes)) {
            return result;
        }
        for (String dictType : dictTypes) {
            if (StringUtils.isBlank(dictType)) {
                continue;
            }
            result.put(dictType.trim(), listByDictType(dictType));
        }
        return result;
    }

    @Override
    public List<DictTypeOptionVO> listActiveDictTypes() {
        return listActiveDictTypes(null);
    }

    @Override
    public List<DictTypeOptionVO> listActiveDictTypes(String scope) {
        String tenantId = resolveTenantId();
        List<DictTypeEntity> types = TenantScopeRunner.runIgnoringTenant(
                () -> dictTypeMapper.selectActiveByTenant(tenantId));
        if (CollectionUtils.isEmpty(types)) {
            return Collections.emptyList();
        }
        Map<String, DictTypeOptionVO> merged = new LinkedHashMap<>();
        for (DictTypeEntity entity : types) {
            if (entity == null || StringUtils.isBlank(entity.getDictType())) {
                continue;
            }
            String key = entity.getDictType().trim();
            if (!DictTypeScope.matches(scope, key)) {
                continue;
            }
            DictTypeOptionVO option = new DictTypeOptionVO(key, entity.getDictName());
            if (TenantConstants.PLATFORM_TENANT_ID.equals(entity.getTenantId())) {
                merged.putIfAbsent(key, option);
            } else {
                merged.put(key, option);
            }
        }
        return merged.values().stream().toList();
    }

    private static String resolveTenantId() {
        return StringUtils.defaultIfBlank(TenantContextHolder.getTenantId(), TenantConstants.PLATFORM_TENANT_ID);
    }

    private DictDataVO toVo(DictItemEntity entity) {
        return new DictDataVO(
                entity.getDictLabel(),
                entity.getDictValue(),
                entity.getListClass(),
                entity.getIcon(),
                entity.getCssClass());
    }
}
