/** AUTO-GENERATED — 字典类型 类型 */

export interface DictTypeVO {
  id?: string
  dictName: string
  dictType: string
  status?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

/** @deprecated use DictTypeVO */
export type SysDictType = DictTypeVO

/** AUTO-GENERATED — 字典数据 类型 */

export interface DictItemVO {
  id?: string
  dictTypeId?: string
  dictLabel: string
  dictValue: string
  valueType?: string
  cssClass?: string
  icon?: string
  listClass?: string
  isDefault?: string
  sortNo?: number
  status?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

/** @deprecated use DictItemVO */
export type SysDictItem = DictItemVO

export interface DictTypePageQuery {
  pageNo?: number
  pageSize?: number
  dictName?: string
  dictType?: string
  status?: string
}

export interface DictItemPageQuery {
  pageNo?: number
  pageSize?: number
  dictTypeId?: string
  dictLabel?: string
  dictValue?: string
  status?: string
}

export interface DictTypeForm {
  id?: string
  dictName: string
  dictType: string
  status?: string
  remark?: string
}

export interface DictItemForm {
  id?: string
  dictTypeId?: string
  dictLabel: string
  dictValue: string
  valueType?: string
  cssClass?: string
  icon?: string
  listClass?: string
  isDefault?: string
  sortNo?: number
  status?: string
  remark?: string
}

/** Runtime dict option for select / radio / tag / catalog icon. */
export interface DictOption {
  label: string
  value: string
  tagType?: string
  icon?: string
  cssClass?: string
}
