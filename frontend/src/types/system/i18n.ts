export interface I18nLocale {
  id?: string
  localeCode: string
  localeName: string
  nativeName: string
  sortNo?: number
  isDefault?: string
  status?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface I18nLocalePageQuery {
  pageNo?: number
  pageSize?: number
  localeCode?: string
  localeName?: string
  status?: string
}

export interface I18nLocaleForm {
  id?: string
  localeCode: string
  localeName: string
  nativeName: string
  sortNo?: number
  isDefault?: string
  status?: string
  remark?: string
}

export interface I18nMessage {
  id?: string
  messageKey: string
  locale: string
  messageValue: string
  module?: string
  scope: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface I18nMessagePageQuery {
  pageNo?: number
  pageSize?: number
  messageKey?: string
  locale?: string
  module?: string
  scope?: string
}

export interface I18nMessageForm {
  id?: string
  messageKey: string
  locale: string
  messageValue: string
  module?: string
  scope: string
  remark?: string
}

export interface I18nBootstrap {
  defaultLocale: string
  supportedLocales: string[]
  locales: I18nLocale[]
}
