export interface MenuRouter {
  id: string
  menuName: string
  routeName?: string
  path: string
  /** Resolved view path from backend MenuRouterVO.component */
  component?: string
  /** Fallback when API uses entity field name */
  componentPath?: string
  icon?: string
  permissionCode?: string
  menuType?: string
  /** 0=侧栏显示 1=侧栏隐藏 */
  visible?: string
  sortNo?: number
  productCode?: string
  children?: MenuRouter[]
}

/** AUTO-GENERATED — 菜单 类型 */

export interface MenuVO {
  id?: string
  parentId?: string
  menuType?: string
  menuName: string
  /** locale -> label from sys_menu.name_i18n */
  nameI18n?: Record<string, string>
  /** Form-only convenience; mapped from/to nameI18n['en-US'] */
  menuNameEn?: string
  routeName?: string
  routePath?: string
  componentPath?: string
  permissionCode?: string
  icon?: string
  sortNo?: number
  status?: string
  remark?: string
  assignScope?: string
  productCode?: string
  children?: MenuVO[]
  createdAt?: string
  updatedAt?: string
}

/** @deprecated use MenuVO */
export type SysMenu = MenuVO

export interface MenuPageQuery {
  pageNo?: number
  pageSize?: number
  menuName?: string
  menuType?: string
  status?: string
  parentId?: string
  /** 角色授权：排除平台专属菜单 */
  assignableOnly?: boolean
}

export interface MenuForm {
  id?: string
  parentId?: string
  menuType?: string
  menuName: string
  nameI18n?: Record<string, string>
  menuNameEn?: string
  routeName?: string
  routePath?: string
  componentPath?: string
  permissionCode?: string
  icon?: string
  sortNo?: number
  status?: string
  remark?: string
  assignScope?: string
  productCode?: string
}
