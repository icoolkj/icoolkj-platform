package com.icoolkj.platform.system.menu.service.impl;

import com.icoolkj.platform.common.constant.MenuAssignScope;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.system.menu.convert.MenuConvert;
import com.icoolkj.platform.system.menu.domain.MenuEntity;
import com.icoolkj.platform.system.menu.dto.MenuCreateRequest;
import com.icoolkj.platform.system.menu.dto.MenuPageQuery;
import com.icoolkj.platform.system.menu.dto.MenuUpdateRequest;
import com.icoolkj.platform.system.menu.mapper.MenuMapper;
import com.icoolkj.platform.system.menu.service.MenuAssignScopeService;
import com.icoolkj.platform.system.role.mapper.RoleMenuMapper;
import com.icoolkj.platform.system.menu.support.MenuProductRules;
import com.icoolkj.platform.system.menu.service.MenuService;
import com.icoolkj.platform.system.menu.vo.MenuVO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 菜单权限资源表服务实现 */
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuAssignScopeService menuAssignScopeService;
    private final MenuProductRules menuProductRules;

    public MenuServiceImpl(MenuMapper menuMapper,
                           RoleMenuMapper roleMenuMapper,
                           MenuAssignScopeService menuAssignScopeService,
                           MenuProductRules menuProductRules) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuAssignScopeService = menuAssignScopeService;
        this.menuProductRules = menuProductRules;
    }

    @Override
    public PageResult<MenuVO> page(MenuPageQuery query) {
        applyAssignableOnlyFilter(query);
        long total = menuMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<MenuEntity> list = menuMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        Locale locale = LocaleContextHolder.getLocale();
        String localeTag = locale != null ? locale.toLanguageTag() : null;
        return PageResult.of(
                list.stream().map(m -> MenuConvert.toLocalizedVo(m, localeTag)).toList(),
                total,
                query.getPageNo(),
                query.getPageSize());
    }

    @Override
    public MenuVO getById(String id) {
        MenuEntity entity = menuMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.menu");
        }
        return MenuConvert.toVo(entity);
    }

    @Override
    public String create(MenuCreateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        MenuEntity entity = MenuConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        menuProductRules.validateForCreate(entity);
        menuMapper.insert(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    public void update(MenuUpdateRequest request) {
        menuAssignScopeService.assertPlatformMenuOperator();
        MenuEntity entity = menuMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.menu");
        }
        String previousScope = entity.getAssignScope();
        String entityId = entity.getId();
        String previousProductCode = entity.getProductCode();
        MenuConvert.copy(request, entity);
        menuProductRules.validateForUpdate(entity, entityId, previousProductCode);
        menuMapper.updateById(entity);
        if (MenuAssignScope.isPlatform(entity.getAssignScope())
                && !Objects.equals(previousScope, entity.getAssignScope())) {
            menuAssignScopeService.onMenuMarkedPlatform(entityId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        menuAssignScopeService.assertPlatformMenuOperator();
        MenuEntity entity = menuMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.menu");
        }
        List<MenuEntity> toDelete = new ArrayList<>();
        toDelete.add(entity);
        collectDescendants(id, toDelete);
        List<String> menuIds = toDelete.stream().map(MenuEntity::getId).toList();
        for (MenuEntity item : toDelete) {
            AuditFieldHelper.markDeleted(item);
            menuMapper.updateById(item);
        }
        roleMenuMapper.deleteByMenuIds(menuIds);
    }

    /** 递归收集子目录 / 子菜单 / 按钮，删除菜单时联动软删。 */
    private void collectDescendants(String parentId, List<MenuEntity> bucket) {
        MenuEntity query = new MenuEntity();
        query.setParentId(parentId);
        List<MenuEntity> children = menuMapper.selectList(query);
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        for (MenuEntity child : children) {
            bucket.add(child);
            collectDescendants(child.getId(), bucket);
        }
    }

    private void applyAssignableOnlyFilter(MenuPageQuery query) {
        if (Boolean.TRUE.equals(query.getAssignableOnly()) && !StringUtils.hasText(query.getAssignScope())) {
            query.setAssignScope(MenuAssignScope.TENANT);
        }
    }
}
