/** Site marketing view.json — structure only; copy lives in zh-CN/en-US JSON. */

export type SiteCta =
  | { to: string; labelKey: string; action?: never }
  | { action: 'console'; labelKey: string; to?: never }

export type SiteViewSection = {
  id: string
  type: string
  ctaPrimary?: SiteCta
  ctaSecondary?: SiteCta
  cta?: SiteCta
  docsTo?: string
  itemIds?: string[]
  items?: { id: string; to: string }[]
  sourcePrefix?: string
}

export type SiteView = {
  page: {
    layout: string
    i18nPrefix: string
  }
  sections: SiteViewSection[]
  related?: { id: string; to: string; labelKey: string }[]
}
