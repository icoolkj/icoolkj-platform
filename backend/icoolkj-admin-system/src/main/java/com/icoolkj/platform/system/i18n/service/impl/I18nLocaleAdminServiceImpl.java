package com.icoolkj.platform.system.i18n.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.system.i18n.convert.I18nConvert;
import com.icoolkj.platform.system.i18n.domain.I18nLocaleEntity;
import com.icoolkj.platform.system.i18n.dto.I18nLocaleCreateRequest;
import com.icoolkj.platform.system.i18n.dto.I18nLocalePageQuery;
import com.icoolkj.platform.system.i18n.dto.I18nLocaleUpdateRequest;
import com.icoolkj.platform.system.i18n.mapper.I18nLocaleMapper;
import com.icoolkj.platform.system.i18n.service.I18nCacheService;
import com.icoolkj.platform.system.i18n.service.I18nLocaleAdminService;
import com.icoolkj.platform.system.menu.service.MenuAssignScopeService;
import com.icoolkj.platform.system.i18n.vo.I18nLocaleVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class I18nLocaleAdminServiceImpl implements I18nLocaleAdminService {

    private final I18nLocaleMapper i18nLocaleMapper;
    private final I18nCacheService i18nCacheService;
    private final MenuAssignScopeService menuAssignScopeService;

    public I18nLocaleAdminServiceImpl(I18nLocaleMapper i18nLocaleMapper,
                                      I18nCacheService i18nCacheService,
                                      MenuAssignScopeService menuAssignScopeService) {
        this.i18nLocaleMapper = i18nLocaleMapper;
        this.i18nCacheService = i18nCacheService;
        this.menuAssignScopeService = menuAssignScopeService;
    }

    @Override
    public PageResult<I18nLocaleVO> page(I18nLocalePageQuery query) {
        menuAssignScopeService.assertPlatformMenuOperator();
        long total = i18nLocaleMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<I18nLocaleEntity> list = i18nLocaleMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        return PageResult.of(list.stream().map(I18nConvert::toLocaleVo).toList(), total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public I18nLocaleVO getById(String id) {
        menuAssignScopeService.assertPlatformMenuOperator();
        I18nLocaleEntity entity = i18nLocaleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.i18n.locale.not_found");
        }
        return I18nConvert.toLocaleVo(entity);
    }

    @Override
    public String create(I18nLocaleCreateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        if (i18nLocaleMapper.selectByCode(request.getLocaleCode()) != null) {
            throw new BusinessException("error.i18n.locale.duplicate");
        }
        I18nLocaleEntity entity = I18nConvert.toLocaleEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        if ("Y".equalsIgnoreCase(entity.getIsDefault())) {
            i18nLocaleMapper.clearDefaultFlag();
        }
        i18nLocaleMapper.insert(entity);
        evictLocaleCache(entity.getLocaleCode());
        return entity.getId();
    }

    @Override
    public void update(I18nLocaleUpdateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        I18nLocaleEntity entity = i18nLocaleMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.i18n.locale.not_found");
        }
        I18nLocaleEntity duplicate = i18nLocaleMapper.selectByCode(request.getLocaleCode());
        if (duplicate != null && !duplicate.getId().equals(entity.getId())) {
            throw new BusinessException("error.i18n.locale.duplicate");
        }
        String oldCode = entity.getLocaleCode();
        if ("Y".equalsIgnoreCase(request.getIsDefault())) {
            i18nLocaleMapper.clearDefaultFlag();
        }
        I18nConvert.copy(request, entity);
        i18nLocaleMapper.updateById(entity);
        evictLocaleCache(oldCode);
        if (!oldCode.equals(entity.getLocaleCode())) {
            evictLocaleCache(entity.getLocaleCode());
        }
    }

    @Override
    public void deleteById(String id) {
        menuAssignScopeService.assertPlatformMenuOperator();
        I18nLocaleEntity entity = i18nLocaleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.i18n.locale.not_found");
        }
        if ("Y".equalsIgnoreCase(entity.getIsDefault())) {
            throw new BusinessException("error.i18n.locale.default_delete");
        }
        AuditFieldHelper.markDeleted(entity);
        i18nLocaleMapper.updateById(entity);
        evictLocaleCache(entity.getLocaleCode());
    }

    private void evictLocaleCache(String localeCode) {
        i18nCacheService.evictEnabledLocales();
        if (StringUtils.hasText(localeCode)) {
            i18nCacheService.evictMessages(localeCode);
        }
    }
}
