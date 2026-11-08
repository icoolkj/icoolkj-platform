/** Shared docs content shape (copy lives in i18n/docs/*.json). */
export type DocSection = {
  title: string
  body: string
  bullets?: string[]
}

export type DocCategory = {
  id: string
  title: string
  summary: string
  sections: DocSection[]
}
