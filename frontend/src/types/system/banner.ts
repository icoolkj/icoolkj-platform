/** 轮播图 */

export interface BannerPositionConfigItem {
  position: string
  clickEnabled: boolean
  linkType: string
  linkUrl?: string
}

export interface BannerPositionConfigItem {
  position: string
  clickEnabled: boolean
  linkType: string
  linkUrl?: string
}

export interface BannerListVO {
  id?: string
  bannerTitle?: string
  imageFileId?: string
  imagePreviewUrl?: string
  displayPositions?: string
  sortNo?: number
  status?: string
  remark?: string
  createdAt?: string
}

export interface BannerVO extends BannerListVO {
  positionConfig?: string
}

export interface BannerPublishedVO {
  id?: string
  bannerTitle?: string
  imageFileId?: string
  imagePreviewUrl?: string
  displayPositions?: string
  positionConfig?: string
  sortNo?: number
  createdAt?: string
}

export interface BannerPageQuery {
  pageNo?: number
  pageSize?: number
  bannerTitle?: string
  displayPosition?: string
  status?: string
}

export interface BannerPublishedQuery {
  displayPosition?: string
  limit?: number
}

export interface BannerForm {
  id?: string
  bannerTitle?: string
  imageFileId?: string
  imagePreviewUrl?: string
  displayPositions?: string
  positionConfig?: string
  sortNo?: number
  status?: string
  remark?: string
}

export interface BannerBatchDeletePayload {
  ids: string[]
}

export interface BannerStatusPayload {
  id: string
  status: string
}
