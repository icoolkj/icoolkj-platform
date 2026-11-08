/**
 * 与后端 CommonConstants / YesNoEnum / StatusEnum / GenderEnum / TenantConstants 对齐的 char(1) 约定。
 * 业务代码禁止再散落写死 '0'/'1'/'2'（业务标志类）。
 */
export const YES = '1'
export const NO = '0'

/** 通用业务状态：正常 / 启用 */
export const STATUS_NORMAL = '0'
/** 通用业务状态：停用 */
export const STATUS_DISABLE = '1'

/** 树形根节点 parentId（菜单等） */
export const TREE_ROOT_PARENT_ID = '0'

/** 平台租户 ID（与后端 TenantConstants.PLATFORM_TENANT_ID 一致） */
export const PLATFORM_TENANT_ID = '0'

/** 代码生成：zip 下载 */
export const GEN_ZIP = '0'
/** 代码生成：自定义路径 */
export const GEN_PATH = '1'

/** 横幅链接类型：无链接（字典 sys_banner_link_type） */
export const BANNER_LINK_NONE = '0'

/** 性别：男（字典 sys_gender，对齐 GenderEnum） */
export const GENDER_MALE = '0'
/** 性别：女 */
export const GENDER_FEMALE = '1'
/** 性别：未知（用户表默认） */
export const GENDER_UNKNOWN = '2'

export function isYes(value: string | null | undefined): boolean {
  return value === YES
}

export function isNo(value: string | null | undefined): boolean {
  return value === NO
}

export function isStatusNormal(value: string | null | undefined): boolean {
  return value === STATUS_NORMAL
}

export function isStatusDisabled(value: string | null | undefined): boolean {
  return value === STATUS_DISABLE
}

export function isPlatformTenant(tenantId: string | null | undefined): boolean {
  return tenantId === PLATFORM_TENANT_ID
}

export function isGenCustomPath(genType: string | null | undefined): boolean {
  return genType === GEN_PATH
}

export function isGenderUnknown(value: string | null | undefined): boolean {
  return value === GENDER_UNKNOWN
}
