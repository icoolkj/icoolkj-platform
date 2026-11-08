/** AUTO-GENERATED — 行政区划 类型 */

export interface RegionVO {
  id?: string
  parentId?: string
  ancestors?: string
  regionCode?: string
  regionName?: string
  regionLevel?: string
  sortNo?: number
  status?: string
  remark?: string
}

export interface RegionPageQuery {
  pageNo?: number
  pageSize?: number
  parentId?: string
  regionCode?: string
  regionName?: string
  regionLevel?: string
  sortNo?: number
  status?: string
}

export interface RegionForm {
  id?: string
  parentId?: string
  regionCode?: string
  regionName?: string
  regionLevel?: string
  sortNo?: number
  status?: string
  remark?: string
}
