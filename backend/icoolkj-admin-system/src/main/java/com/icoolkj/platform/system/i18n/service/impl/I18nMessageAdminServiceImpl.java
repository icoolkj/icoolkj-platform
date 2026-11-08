package com.icoolkj.platform.system.i18n.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.system.i18n.convert.I18nConvert;
import com.icoolkj.platform.system.i18n.domain.I18nMessageEntity;
import com.icoolkj.platform.system.i18n.dto.I18nMessageCreateRequest;
import com.icoolkj.platform.system.i18n.dto.I18nMessagePageQuery;
import com.icoolkj.platform.system.i18n.dto.I18nMessageUpdateRequest;
import com.icoolkj.platform.system.i18n.mapper.I18nMessageMapper;
import com.icoolkj.platform.system.i18n.service.I18nCacheService;
import com.icoolkj.platform.system.i18n.service.I18nMessageAdminService;
import com.icoolkj.platform.system.menu.service.MenuAssignScopeService;
import com.icoolkj.platform.system.i18n.vo.I18nMessageVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Service
public class I18nMessageAdminServiceImpl implements I18nMessageAdminService {

    private final I18nMessageMapper i18nMessageMapper;
    private final I18nCacheService i18nCacheService;
    private final MenuAssignScopeService menuAssignScopeService;

    public I18nMessageAdminServiceImpl(I18nMessageMapper i18nMessageMapper,
                                       I18nCacheService i18nCacheService,
                                       MenuAssignScopeService menuAssignScopeService) {
        this.i18nMessageMapper = i18nMessageMapper;
        this.i18nCacheService = i18nCacheService;
        this.menuAssignScopeService = menuAssignScopeService;
    }

    @Override
    public PageResult<I18nMessageVO> page(I18nMessagePageQuery query) {
        menuAssignScopeService.assertPlatformMenuOperator();
        long total = i18nMessageMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<I18nMessageEntity> list = i18nMessageMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        return PageResult.of(list.stream().map(I18nConvert::toMessageVo).toList(), total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public I18nMessageVO getById(String id) {
        menuAssignScopeService.assertPlatformMenuOperator();
        I18nMessageEntity entity = i18nMessageMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.i18n.message.not_found");
        }
        return I18nConvert.toMessageVo(entity);
    }

    @Override
    public String create(I18nMessageCreateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        if (i18nMessageMapper.selectByKeyAndLocale(request.getMessageKey(), request.getLocale()) != null) {
            throw new BusinessException("error.i18n.message.duplicate");
        }
        I18nMessageEntity entity = I18nConvert.toMessageEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        i18nMessageMapper.insert(entity);
        i18nCacheService.evictMessages(entity.getLocale());
        return entity.getId();
    }

    @Override
    public void update(I18nMessageUpdateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        I18nMessageEntity entity = i18nMessageMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.i18n.message.not_found");
        }
        I18nMessageEntity duplicate = i18nMessageMapper.selectByKeyAndLocale(request.getMessageKey(), request.getLocale());
        if (duplicate != null && !duplicate.getId().equals(entity.getId())) {
            throw new BusinessException("error.i18n.message.duplicate");
        }
        String oldLocale = entity.getLocale();
        I18nConvert.copy(request, entity);
        i18nMessageMapper.updateById(entity);
        i18nCacheService.evictMessages(oldLocale);
        if (!oldLocale.equals(entity.getLocale())) {
            i18nCacheService.evictMessages(entity.getLocale());
        }
    }

    @Override
    public void deleteById(String id) {
        menuAssignScopeService.assertPlatformMenuOperator();
        I18nMessageEntity entity = i18nMessageMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.i18n.message.not_found");
        }
        i18nMessageMapper.deleteById(id);
        i18nCacheService.evictMessages(entity.getLocale());
    }

    @Override
    public void refreshCache() {
        menuAssignScopeService.assertPlatformMenuOperator();
        i18nCacheService.refreshAllMessages();
        i18nCacheService.evictEnabledLocales();
    }
}
