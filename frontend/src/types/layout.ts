export type SidebarTheme = 'dark' | 'light'
export type FormMode = 'dialog' | 'drawer'
export type HeaderStyle = 'light' | 'primary'

export interface LayoutSettings {
  sidebarTheme: SidebarTheme
  /** Top shell bar: light (white) or primary (theme-tinted, matches sidebar) */
  headerStyle: HeaderStyle
  breadcrumb: boolean
  tagsView: boolean
  formMode: FormMode
  watermark: boolean
  menuUniqueOpened: boolean
  /** Show page title + description block on CRUD pages */
  pageHeader: boolean
}

export interface TagView {
  path: string
  fullPath: string
  title: string
  affix?: boolean
}

/** Max open tabs; oldest non-affix tab is evicted when exceeded. */
export const TAGS_VIEW_MAX = 15
