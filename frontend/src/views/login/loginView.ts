/**
 * 登录页壳层 view.json。
 * 固定展位一律 catalogCode + profileCode（或整目录 catalogCode），禁止只写槽位名。
 */
import loginViewJson from '@/i18n/shell/login/view.json'

export type LoginFieldWhen = 'requireCaptcha' | 'mfaStep' | '!mfaStep'

export interface LoginViewFeature {
  titleKey: string
  descKey: string
}

export interface LoginViewFormField {
  field: string
  component: 'input' | 'password' | 'captcha' | 'mfa' | 'checkbox' | 'agreement'
  when?: LoginFieldWhen
}

/** 单槽位表面：必须带目录 + 槽位 */
export interface LoginViewSlotSurface {
  catalogCode: string
  profileCode: string
}

/** 整目录表面（如二维码：启用且有图的槽位全展示） */
export interface LoginViewCatalogSurface {
  catalogCode: string
  defaultLabelKey?: string
}

export interface LoginViewBrandSurfaces {
  logo?: LoginViewSlotSurface
  favicon?: LoginViewSlotSurface
  qr?: LoginViewCatalogSurface
}

export interface LoginViewConfig {
  page: {
    layout: 'split-brand-form'
    brandSide: 'left' | 'right'
    columns: {
      brand: number
      form: number
    }
  }
  brand: {
    showLogo: boolean
    showIntro: boolean
    showCopyrightFooter: boolean
    showTestAccountsLink: boolean
    resolveByTenant?: boolean
    surfaces?: LoginViewBrandSurfaces
    features: LoginViewFeature[]
  }
  form: {
    showLocaleSwitcher: boolean
    showEmailFooter: boolean
    fields: LoginViewFormField[]
  }
}

export const loginView = loginViewJson as LoginViewConfig

export function isLoginFieldVisible(
  field: LoginViewFormField,
  ctx: { requireCaptcha: boolean; mfaStep: boolean },
): boolean {
  if (!field.when) {
    return true
  }
  if (field.when === 'requireCaptcha') {
    return ctx.requireCaptcha && !ctx.mfaStep
  }
  if (field.when === 'mfaStep') {
    return ctx.mfaStep
  }
  if (field.when === '!mfaStep') {
    return !ctx.mfaStep
  }
  return true
}
