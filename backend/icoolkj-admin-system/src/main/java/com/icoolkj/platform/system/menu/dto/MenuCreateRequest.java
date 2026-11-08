package com.icoolkj.platform.system.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/** 新增菜单权限资源表 */
@Data
public class MenuCreateRequest {
    @NotBlank(message = "{gen.menu.parentId.required}")
    @Size(max = 20, message = "{gen.menu.parentId.maxLength}")
    private String parentId;
    @NotBlank(message = "{gen.menu.menuType.required}")
    @Size(max = 1, message = "{gen.menu.menuType.maxLength}")
    private String menuType;
    @NotBlank(message = "{gen.menu.menuName.required}")
    @Size(max = 50, message = "{gen.menu.menuName.maxLength}")
    private String menuName;
    /** 多语言名称 locale -> label；写入 sys_menu.name_i18n */
    private Map<String, String> nameI18n;
    @Size(max = 100, message = "{gen.menu.routeName.maxLength}")
    private String routeName;
    @Size(max = 200, message = "{gen.menu.routePath.maxLength}")
    private String routePath;
    @Size(max = 200, message = "{gen.menu.componentPath.maxLength}")
    private String componentPath;
    @Size(max = 100, message = "{gen.menu.permissionCode.maxLength}")
    private String permissionCode;
    @Size(max = 100, message = "{gen.menu.icon.maxLength}")
    private String icon;
    @NotNull(message = "{gen.menu.sortNo.required}")
    private Integer sortNo;
    @NotBlank(message = "{gen.menu.visible.required}")
    @Size(max = 1, message = "{gen.menu.visible.maxLength}")
    private String visible;
    @NotBlank(message = "{gen.menu.status.required}")
    @Size(max = 1, message = "{gen.menu.status.maxLength}")
    private String status;
    @NotBlank(message = "{gen.menu.isExternal.required}")
    @Size(max = 1, message = "{gen.menu.isExternal.maxLength}")
    private String isExternal;
    @NotBlank(message = "{gen.menu.isCache.required}")
    @Size(max = 1, message = "{gen.menu.isCache.maxLength}")
    private String isCache;
    @Size(max = 500, message = "{gen.menu.remark.maxLength}")
    private String remark;
    @Size(max = 20, message = "{gen.menu.assignScope.maxLength}")
    private String assignScope;
    @Size(max = 32)
    private String productCode;
}
