/** OA 发起中心目录项 */
export interface OaInitiateCatalogItemVO {
  tenantTemplateId: string
  categoryCode: string
  templateCode: string
  templateName: string
  publishedTplVerId?: string
  publishedVersionNo?: number
  remark?: string
}
