package com.icoolkj.platform.system.menu.service.impl;

import com.icoolkj.platform.system.menu.domain.MenuEntity;
import com.icoolkj.platform.system.menu.mapper.MenuMapper;
import com.icoolkj.platform.system.role.mapper.RoleMenuMapper;
import com.icoolkj.platform.system.menu.service.MenuAssignScopeService;
import com.icoolkj.platform.system.menu.support.MenuProductRules;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplDeleteCascadeTest {

    @Mock
    private MenuMapper menuMapper;
    @Mock
    private RoleMenuMapper roleMenuMapper;
    @Mock
    private MenuAssignScopeService menuAssignScopeService;
    @Mock
    private MenuProductRules menuProductRules;

    private MenuServiceImpl menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuServiceImpl(menuMapper, roleMenuMapper, menuAssignScopeService, menuProductRules);
        doNothing().when(menuAssignScopeService).assertPlatformMenuOperator();
    }

    @Test
    void deleteMenuCascadesButtonsAndCleansRoleMenus() {
        MenuEntity page = menu("c1", "0", "C");
        MenuEntity btnList = menu("f1", "c1", "F");
        MenuEntity btnExport = menu("f2", "c1", "F");

        when(menuMapper.selectById("c1")).thenReturn(page);
        when(menuMapper.selectList(any(MenuEntity.class))).thenAnswer(inv -> {
            MenuEntity query = inv.getArgument(0);
            if ("c1".equals(query.getParentId())) {
                return List.of(btnList, btnExport);
            }
            return List.of();
        });
        when(menuMapper.updateById(any(MenuEntity.class))).thenReturn(1);

        menuService.deleteById("c1");

        ArgumentCaptor<MenuEntity> updateCaptor = ArgumentCaptor.forClass(MenuEntity.class);
        verify(menuMapper, times(3)).updateById(updateCaptor.capture());
        assertTrue(updateCaptor.getAllValues().stream().allMatch(m -> Integer.valueOf(1).equals(m.getDeleted())));

        verify(roleMenuMapper).deleteByMenuIds(List.of("c1", "f1", "f2"));
    }

    @Test
    void deleteDirectoryCascadesNestedMenusAndButtons() {
        MenuEntity dir = menu("m1", "0", "M");
        MenuEntity page = menu("c1", "m1", "C");
        MenuEntity btn = menu("f1", "c1", "F");

        when(menuMapper.selectById("m1")).thenReturn(dir);
        when(menuMapper.selectList(any(MenuEntity.class))).thenAnswer(inv -> {
            MenuEntity query = inv.getArgument(0);
            if ("m1".equals(query.getParentId())) {
                return List.of(page);
            }
            if ("c1".equals(query.getParentId())) {
                return List.of(btn);
            }
            return List.of();
        });
        when(menuMapper.updateById(any(MenuEntity.class))).thenReturn(1);

        menuService.deleteById("m1");

        verify(menuMapper, times(3)).updateById(any(MenuEntity.class));
        verify(roleMenuMapper).deleteByMenuIds(List.of("m1", "c1", "f1"));
    }

    private static MenuEntity menu(String id, String parentId, String menuType) {
        MenuEntity entity = new MenuEntity();
        entity.setId(id);
        entity.setParentId(parentId);
        entity.setMenuType(menuType);
        entity.setDeleted(0);
        return entity;
    }
}
