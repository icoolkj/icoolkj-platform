package com.icoolkj.platform.system.dict.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.system.dict.convert.DictTypeConvert;
import com.icoolkj.platform.system.dict.domain.DictTypeEntity;
import com.icoolkj.platform.system.dict.dto.DictTypeCreateRequest;
import com.icoolkj.platform.system.dict.dto.DictTypePageQuery;
import com.icoolkj.platform.system.dict.dto.DictTypeUpdateRequest;
import com.icoolkj.platform.system.dict.mapper.DictTypeMapper;
import com.icoolkj.platform.system.dict.service.DictTypeService;
import com.icoolkj.platform.system.menu.service.MenuAssignScopeService;
import com.icoolkj.platform.system.dict.vo.DictTypeVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/** 字典类型表服务实现 */
@Service
public class DictTypeServiceImpl implements DictTypeService {

    private final DictTypeMapper dictTypeMapper;
    private final MenuAssignScopeService menuAssignScopeService;

    public DictTypeServiceImpl(DictTypeMapper dictTypeMapper, MenuAssignScopeService menuAssignScopeService) {
        this.dictTypeMapper = dictTypeMapper;
        this.menuAssignScopeService = menuAssignScopeService;
    }

    @Override
    public PageResult<DictTypeVO> page(DictTypePageQuery query) {
        menuAssignScopeService.assertPlatformMenuOperator();
        long total = dictTypeMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<DictTypeEntity> list = dictTypeMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        return PageResult.of(list.stream().map(DictTypeConvert::toVo).toList(), total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public DictTypeVO getById(String id) {
        menuAssignScopeService.assertPlatformMenuOperator();
        DictTypeEntity entity = dictTypeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.dict_type");
        }
        return DictTypeConvert.toVo(entity);
    }

    @Override
    public String create(DictTypeCreateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        assertDictTypeAvailable(request.getDictType(), null);
        DictTypeEntity entity = DictTypeConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        dictTypeMapper.insert(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    public void update(DictTypeUpdateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        DictTypeEntity entity = dictTypeMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.dict_type");
        }
        assertDictTypeAvailable(request.getDictType(), entity.getId());
        DictTypeConvert.copy(request, entity);
        dictTypeMapper.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        menuAssignScopeService.assertPlatformMenuOperator();
        DictTypeEntity entity = dictTypeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.dict_type");
        }
        AuditFieldHelper.markDeleted(entity);
        dictTypeMapper.updateById(entity);
    }

    private void assertDictTypeAvailable(String dictType, String excludeId) {
        if (dictType == null || dictType.isBlank()) {
            return;
        }
        DictTypeEntity query = new DictTypeEntity();
        query.setDictType(dictType.trim());
        List<DictTypeEntity> list = dictTypeMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        boolean duplicate = list.stream().anyMatch(item -> excludeId == null || !excludeId.equals(item.getId()));
        if (duplicate) {
            throw new BusinessException("error.dict_type.code_exists");
        }
    }
}
