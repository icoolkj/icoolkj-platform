package com.icoolkj.platform.system.dict.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.system.dict.convert.DictItemConvert;
import com.icoolkj.platform.system.dict.domain.DictItemEntity;
import com.icoolkj.platform.system.dict.dto.DictItemCreateRequest;
import com.icoolkj.platform.system.dict.dto.DictItemPageQuery;
import com.icoolkj.platform.system.dict.dto.DictItemUpdateRequest;
import com.icoolkj.platform.system.dict.mapper.DictItemMapper;
import com.icoolkj.platform.system.dict.service.DictItemService;
import com.icoolkj.platform.system.menu.service.MenuAssignScopeService;
import com.icoolkj.platform.system.dict.vo.DictItemVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/** 字典数据项表服务实现 */
@Service
public class DictItemServiceImpl implements DictItemService {

    private final DictItemMapper dictItemMapper;
    private final MenuAssignScopeService menuAssignScopeService;

    public DictItemServiceImpl(DictItemMapper dictItemMapper, MenuAssignScopeService menuAssignScopeService) {
        this.dictItemMapper = dictItemMapper;
        this.menuAssignScopeService = menuAssignScopeService;
    }

    @Override
    public PageResult<DictItemVO> page(DictItemPageQuery query) {
        menuAssignScopeService.assertPlatformMenuOperator();
        long total = dictItemMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<DictItemEntity> list = dictItemMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        return PageResult.of(list.stream().map(DictItemConvert::toVo).toList(), total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public DictItemVO getById(String id) {
        menuAssignScopeService.assertPlatformMenuOperator();
        DictItemEntity entity = dictItemMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.dict_item");
        }
        return DictItemConvert.toVo(entity);
    }

    @Override
    public String create(DictItemCreateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        assertDictValueAvailable(request.getDictTypeId(), request.getDictValue(), null);
        DictItemEntity entity = DictItemConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        dictItemMapper.insert(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    public void update(DictItemUpdateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        DictItemEntity entity = dictItemMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.dict_item");
        }
        assertDictValueAvailable(request.getDictTypeId(), request.getDictValue(), entity.getId());
        DictItemConvert.copy(request, entity);
        dictItemMapper.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        menuAssignScopeService.assertPlatformMenuOperator();
        DictItemEntity entity = dictItemMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.dict_item");
        }
        AuditFieldHelper.markDeleted(entity);
        dictItemMapper.updateById(entity);
    }

    private void assertDictValueAvailable(String dictTypeId, String dictValue, String excludeId) {
        if (dictTypeId == null || dictTypeId.isBlank() || dictValue == null || dictValue.isBlank()) {
            return;
        }
        DictItemEntity query = new DictItemEntity();
        query.setDictTypeId(dictTypeId.trim());
        query.setDictValue(dictValue.trim());
        List<DictItemEntity> list = dictItemMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        boolean duplicate = list.stream().anyMatch(item -> excludeId == null || !excludeId.equals(item.getId()));
        if (duplicate) {
            throw new BusinessException("error.dict_item.value_exists");
        }
    }
}
