<template>
  <div
    class="login-page"
    :class="brandSide === 'right' ? 'login-page--brand-right' : 'login-page--brand-left'"
    :style="pageStyle"
  >
    <aside class="login-aside">
      <div class="login-aside__glow" aria-hidden="true" />
      <div class="login-aside__inner">
        <img
          v-if="loginView.brand.showLogo"
          class="login-aside__logo"
          :src="displayLogoSrc"
          :alt="displayProductName"
          @error="onLogoError"
        />
        <h1 class="login-aside__title">{{ displayProductName }}</h1>
        <p class="login-aside__subtitle">{{ displaySubtitle }}</p>
        <p v-if="loginView.brand.showIntro" class="login-aside__intro">{{ t('login.brandIntro') }}</p>

        <ul v-if="loginView.brand.features.length" class="login-aside__features">
          <li v-for="(item, index) in loginView.brand.features" :key="index">
            <strong>{{ t(item.titleKey) }}</strong>
            <span>{{ t(item.descKey) }}</span>
          </li>
        </ul>

        <div v-if="loginQrItems.length" class="login-aside__qr">
          <div
            v-for="item in loginQrItems"
            :key="item.profileCode"
            class="login-aside__qr-item"
          >
            <img
              class="login-aside__qr-img"
              :src="item.src"
              :alt="item.caption"
              @error="onQrError(item.profileCode)"
            />
            <span>{{ item.caption }}</span>
          </div>
        </div>

        <router-link
          v-if="showTestAccountsLink"
          class="login-aside__dev-link"
          :to="devTestAccountsPath"
        >
          {{ t('login.testAccountsLink') }}
        </router-link>
      </div>

      <footer v-if="loginView.brand.showCopyrightFooter" class="login-aside__footer">
        <span class="login-aside__brand">{{ copyright.brand }}</span>
        <span class="login-aside__dot">·</span>
        <a :href="copyright.icpUrl" target="_blank" rel="noopener noreferrer">{{ copyright.icpNo }}</a>
        <span class="login-aside__dot">·</span>
        <a :href="copyright.siteUrl" target="_blank" rel="noopener noreferrer">{{ copyright.domain }}</a>
      </footer>
    </aside>

    <main class="login-main">
      <div class="login-main__panel">
        <div v-if="demoStore.isDemo" class="login-demo-banner">
          <el-alert :title="t('layout.demoBanner')" type="warning" :closable="false" show-icon />
        </div>

        <div class="login-card-header">
          <div>
            <h2 class="login-title">{{ t('login.title') }}</h2>
            <p class="login-subtitle">{{ t('login.subtitle') }}</p>
          </div>
          <LocaleSwitcher v-if="loginView.form.showLocaleSwitcher" />
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          :validate-on-rule-change="false"
          label-position="top"
          size="large"
          autocomplete="off"
          @submit.prevent
        >
          <template v-for="field in visibleFormFields" :key="field.field">
            <el-form-item
              v-if="field.component === 'input' && field.field === 'tenantCode'"
              :label="t('login.tenantCode')"
              prop="tenantCode"
            >
              <el-input
                v-model="form.tenantCode"
                name="ic-tenant-code"
                autocomplete="off"
                :placeholder="t('login.tenantCodePlaceholder')"
                :disabled="mfaStep"
                @blur="onTenantBlur"
              />
            </el-form-item>
            <el-form-item
              v-else-if="field.component === 'input' && field.field === 'username'"
              :label="t('login.username')"
              prop="username"
            >
              <el-input
                v-model="form.username"
                name="ic-username"
                autocomplete="username"
                :placeholder="t('login.usernamePlaceholder')"
                :disabled="mfaStep"
              />
            </el-form-item>
            <el-form-item
              v-else-if="field.component === 'password'"
              :label="t('login.password')"
              prop="password"
            >
              <el-input
                v-model="form.password"
                name="ic-password"
                type="password"
                show-password
                autocomplete="current-password"
                :placeholder="t('login.passwordRequired')"
                :disabled="mfaStep"
              />
            </el-form-item>
            <el-form-item
              v-else-if="field.component === 'captcha'"
              :label="t('login.captcha')"
              prop="captchaCode"
            >
              <div class="login-captcha">
                <el-input
                  v-model="form.captchaCode"
                  :placeholder="t('login.captchaPlaceholder')"
                  maxlength="8"
                  @keyup.enter="handleLogin"
                />
                <button
                  type="button"
                  class="login-captcha__image"
                  :title="t('login.captchaRefresh')"
                  :disabled="captchaLoading"
                  @click="refreshCaptcha(true)"
                >
                  <img v-if="captchaImage" :src="captchaImage" alt="captcha" />
                  <span v-else>{{ t('login.captchaRefresh') }}</span>
                </button>
              </div>
            </el-form-item>
            <el-form-item
              v-else-if="field.component === 'mfa'"
              :label="t('login.mfaCode')"
              prop="mfaCode"
            >
              <el-input
                v-model="form.mfaCode"
                :placeholder="t('login.mfaCodePlaceholder')"
                maxlength="6"
                @keyup.enter="handleMfaVerify"
              />
            </el-form-item>
            <el-form-item
              v-else-if="field.component === 'checkbox'"
              class="login-remember-item"
            >
              <el-checkbox v-model="form.rememberLogin">{{ t('login.rememberLogin') }}</el-checkbox>
            </el-form-item>
            <el-form-item
              v-else-if="field.component === 'agreement'"
              prop="agreeFramework"
              class="login-agree-item"
            >
              <div class="login-agree">
                <el-checkbox v-model="form.agreeFramework">
                  <span class="login-agree__text">
                    {{ t('login.agreementPrefix') }}
                    <button type="button" class="login-agree__link" @click.stop.prevent="openAgreement">
                      {{ t('login.agreementLink') }}
                    </button>
                  </span>
                </el-checkbox>
              </div>
            </el-form-item>
          </template>

          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="mfaStep ? handleMfaVerify() : handleLogin()"
          >
            {{ mfaStep ? t('login.mfaVerify') : t('login.login') }}
          </el-button>
          <el-button
            v-if="mfaStep"
            class="login-btn login-btn--ghost"
            :disabled="loading"
            @click="cancelMfaStep"
          >
            {{ t('login.mfaBack') }}
          </el-button>
        </el-form>

        <p v-if="loginView.form.showEmailFooter" class="login-main__foot">
          {{ t('login.copyrightEmailLabel') }}：
          <a :href="`mailto:${copyright.email}`">{{ copyright.email }}</a>
        </p>
      </div>
    </main>

    <el-dialog
      v-model="agreementVisible"
      :title="t('login.agreementDialogTitle')"
      width="640px"
      class="login-agreement-dialog"
      append-to-body
      destroy-on-close
    >
      <div class="login-agreement-body">
        <p class="login-agreement-version">{{ t('login.agreementVersion') }}</p>
        <p class="login-agreement-intro">{{ t('login.agreementIntro') }}</p>
        <section
          v-for="(section, index) in agreementSections"
          :key="index"
          class="login-agreement-section"
        >
          <h3>{{ section.title }}</h3>
          <p>{{ section.body }}</p>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="acceptAgreement">{{ t('login.agreementClose') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import LocaleSwitcher from '@/components/LocaleSwitcher.vue'
import platformLogo from '@/assets/brand/platform-logo.png'
import { fetchCaptcha, fetchLoginBrand, type LoginBrand } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import { useDemoStore } from '@/stores/demo'
import { useBrandStore } from '@/stores/brand'
import { useProductStore } from '@/stores/product'
import { findFirstRegisteredPath, setupDynamicRoutes } from '@/router/dynamicRoutes'
import { DEV_TEST_ACCOUNTS_PATH, TEST_PASSWORD } from '@/dev/test-accounts/data'
import {
  clearLoginRemember,
  loadLoginRemember,
  saveLoginRemember,
} from '@/utils/loginRemember'
import { applyBrandDocumentTitle, applyBrandFavicon } from '@/utils/brandChrome'
import { isLoginFieldVisible, loginView } from './loginView'

const { t, locale, tm } = useI18n()
const brandStore = useBrandStore()
const copyright = computed(() => brandStore.copyrightForLocale(locale.value))
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const menuStore = useMenuStore()
const demoStore = useDemoStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const agreementVisible = ref(false)

const agreementSections = computed(
  () => tm('login.agreementSections') as { title: string; body: string }[],
)

const brandSide = computed(() => loginView.page.brandSide || 'left')
const pageStyle = computed(() => ({
  '--login-brand-fr': `${loginView.page.columns?.brand ?? 1.22}fr`,
  '--login-form-fr': `${loginView.page.columns?.form ?? 1}fr`,
}))

/** view 开关 + 环境（demo/dev）；后期去掉本链接 + 删除 @/dev/test-accounts */
const showTestAccountsLink = computed(
  () =>
    !!loginView.brand.showTestAccountsLink && (demoStore.isDemo || import.meta.env.DEV),
)
const devTestAccountsPath = DEV_TEST_ACCOUNTS_PATH

const resolveBrandByTenant = computed(() => loginView.brand.resolveByTenant !== false)
const tenantBrand = ref<LoginBrand | null>(null)
const logoBroken = ref(false)
const qrBrokenSlots = ref<Set<string>>(new Set())
let lastBrandTenant = ''

function toApiAssetUrl(path: string) {
  if (/^https?:\/\//i.test(path) || path.startsWith('data:')) {
    return path
  }
  if (path.startsWith('/api/')) {
    return path
  }
  return `/api${path.startsWith('/') ? path : `/${path}`}`
}

/** 按 目录+槽位 取公开图；有 brand 响应即可（含平台回退，不强制 tenantMatched） */
function assetUrl(
  catalogCode: string | undefined | null,
  profileCode: string | undefined | null,
): string | null {
  if (!catalogCode || !profileCode || !tenantBrand.value) {
    return null
  }
  const url = tenantBrand.value.assets?.[catalogCode]?.[profileCode]?.trim()
  return url ? toApiAssetUrl(url) : null
}

const surfaces = computed(() => loginView.brand.surfaces || {})

const displayProductName = computed(() => {
  const brand = tenantBrand.value
  if (brand?.tenantMatched) {
    const localized = brand.productNameI18n?.[locale.value]
    if (localized?.trim()) {
      return localized.trim()
    }
    if (brand.productName?.trim()) {
      return brand.productName.trim()
    }
  }
  // 未匹配租户时仍可用平台/回退包上的产品名
  if (brand?.productNameI18n?.[locale.value]?.trim()) {
    return brand.productNameI18n[locale.value]!.trim()
  }
  if (brand?.productName?.trim()) {
    return brand.productName.trim()
  }
  return brandStore.productNameForLocale(locale.value) || t('login.brandTitle')
})

const displaySubtitle = computed(() => {
  const brand = tenantBrand.value
  if (brand?.tenantMatched && brand.tenantName?.trim()) {
    return brand.tenantName.trim()
  }
  return t('login.brandSubtitle')
})

const displayLogoSrc = computed(() => {
  if (logoBroken.value) {
    return platformLogo
  }
  const logo = surfaces.value.logo
  const url = assetUrl(logo?.catalogCode, logo?.profileCode)
  return url || platformLogo
})

/** 目录驱动：有公开素材即展示（租户匹配或平台回退包均可） */
const loginQrItems = computed(() => {
  const qr = surfaces.value.qr
  if (!qr?.catalogCode || !tenantBrand.value) {
    return []
  }
  const catalogCode = qr.catalogCode
  const slots = tenantBrand.value.catalogs?.[catalogCode] || []
  const broken = qrBrokenSlots.value
  const defaultKey = qr.defaultLabelKey || 'login.qrDefault'
  const items: { profileCode: string; src: string; caption: string }[] = []
  for (const profileCode of slots) {
    if (!profileCode || broken.has(profileCode)) {
      continue
    }
    const src = assetUrl(catalogCode, profileCode)
    if (!src) {
      continue
    }
    const label = tenantBrand.value.labels?.[catalogCode]?.[profileCode]?.trim()
    items.push({
      profileCode,
      src,
      caption: label || t(defaultKey),
    })
  }
  return items
})

function onLogoError() {
  logoBroken.value = true
}

function onQrError(profileCode: string) {
  const next = new Set(qrBrokenSlots.value)
  next.add(profileCode)
  qrBrokenSlots.value = next
}

function resetTenantBrand() {
  tenantBrand.value = null
  logoBroken.value = false
  qrBrokenSlots.value = new Set()
  lastBrandTenant = ''
  applyBrandDocumentTitle(locale.value)
  void applyBrandFavicon({
    fileId: brandStore.faviconFileId(),
    publicUrl: null,
  })
}

async function refreshLoginBrand(force = false) {
  if (!resolveBrandByTenant.value) {
    return
  }
  const tenantCode = form.tenantCode?.trim() || ''
  // 空租户也拉平台品牌（二维码/Logo）；用 '' 作为缓存键
  if (!force && tenantCode === lastBrandTenant && tenantBrand.value) {
    return
  }
  try {
    tenantBrand.value = await fetchLoginBrand(tenantCode || undefined)
    logoBroken.value = false
    qrBrokenSlots.value = new Set()
    lastBrandTenant = tenantCode
    applyBrandDocumentTitle(locale.value)
    const favicon = surfaces.value.favicon
    const faviconPublic =
      favicon?.catalogCode && favicon?.profileCode
        ? tenantBrand.value?.assets?.[favicon.catalogCode]?.[favicon.profileCode]?.trim()
        : null
    if (faviconPublic) {
      void applyBrandFavicon({
        publicUrl: toApiAssetUrl(faviconPublic),
        fileId: null,
      })
    } else {
      void applyBrandFavicon({
        fileId: brandStore.faviconFileId(),
      })
    }
    if (tenantBrand.value?.tenantMatched && (tenantBrand.value.productName || tenantBrand.value.productNameI18n)) {
      document.title =
        tenantBrand.value.productNameI18n?.[locale.value]?.trim() ||
        tenantBrand.value.productName?.trim() ||
        brandStore.productNameForLocale(locale.value)
    }
  } catch {
    resetTenantBrand()
  }
}

const form = reactive({
  tenantCode: '',
  username: '',
  password: '',
  captchaCode: '',
  captchaKey: '',
  mfaCode: '',
  rememberLogin: false,
  agreeFramework: false,
})

function hasLoginPrefillQuery() {
  return (
    (typeof route.query.tenantCode === 'string' && !!route.query.tenantCode.trim()) ||
    (typeof route.query.username === 'string' && !!route.query.username.trim())
  )
}

function clearCredentialFields() {
  form.tenantCode = ''
  form.username = ''
  form.password = ''
  form.captchaCode = ''
  form.captchaKey = ''
}

function applyLoginQuery() {
  const tenantCode = typeof route.query.tenantCode === 'string' ? route.query.tenantCode.trim() : ''
  const username = typeof route.query.username === 'string' ? route.query.username.trim() : ''
  // 仅测试账号页跳转带 query 时预填，不写入「记住登录」
  if (!tenantCode && !username) {
    return
  }
  if (tenantCode) {
    form.tenantCode = tenantCode
  }
  if (username) {
    form.username = username
  }
  form.password = TEST_PASSWORD
  form.captchaCode = ''
  form.rememberLogin = false
}

watch(
  () => [route.query.tenantCode, route.query.username] as const,
  () => applyLoginQuery(),
  { immediate: true },
)

const mfaStep = ref(false)
const mfaTicket = ref('')
const requireCaptcha = ref(false)
const captchaImage = ref('')
const captchaLoading = ref(false)

const visibleFormFields = computed(() =>
  loginView.form.fields.filter((field) =>
    isLoginFieldVisible(field, {
      requireCaptcha: requireCaptcha.value,
      mfaStep: mfaStep.value,
    }),
  ),
)

const rules = computed<FormRules>(() => {
  const base: FormRules = {
    tenantCode: [{ required: true, message: t('login.tenantCodeRequired'), trigger: 'blur' }],
    username: [{ required: true, message: t('login.usernameRequired'), trigger: 'blur' }],
    password: [{ required: true, message: t('login.passwordRequired'), trigger: 'blur' }],
  }
  if (mfaStep.value) {
    base.mfaCode = [
      { required: true, message: t('login.mfaCodeRequired'), trigger: 'blur' },
      {
        pattern: /^\d{6}$/,
        message: t('login.mfaCodeRequired'),
        trigger: 'blur',
      },
    ]
  } else {
    base.agreeFramework = [
      {
        validator: (_rule, value, callback) => {
          if (value === true) {
            callback()
          } else {
            callback(new Error(t('login.agreementRequired')))
          }
        },
        trigger: 'change',
      },
    ]
    if (requireCaptcha.value) {
      base.captchaCode = [
        { required: true, message: t('login.captchaPlaceholder'), trigger: 'blur' },
      ]
    }
  }
  return base
})

function openAgreement() {
  agreementVisible.value = true
}

function acceptAgreement() {
  form.agreeFramework = true
  agreementVisible.value = false
  formRef.value?.clearValidate('agreeFramework')
}

let lastCaptchaTenant = ''
/** 避免租户码输入过程中连打 captcha；仅失焦 / 首屏 / 登录失败后拉取 */

async function refreshCaptcha(force = false) {
  const tenantCode = form.tenantCode?.trim()
  if (!tenantCode) {
    requireCaptcha.value = false
    captchaImage.value = ''
    form.captchaKey = ''
    form.captchaCode = ''
    lastCaptchaTenant = ''
    return
  }
  // 同一租户已探测过：非强制时不重复打后端（需换图时传 force=true）
  if (!force && tenantCode === lastCaptchaTenant) {
    return
  }
  captchaLoading.value = true
  try {
    const challenge = await fetchCaptcha(tenantCode)
    lastCaptchaTenant = tenantCode
    requireCaptcha.value = !!challenge.requireCaptcha
    if (challenge.requireCaptcha) {
      form.captchaKey = challenge.captchaKey || ''
      captchaImage.value = challenge.imageBase64 || ''
      form.captchaCode = ''
    } else {
      form.captchaKey = ''
      captchaImage.value = ''
      form.captchaCode = ''
    }
  } catch {
    requireCaptcha.value = false
    captchaImage.value = ''
    form.captchaKey = ''
    lastCaptchaTenant = ''
  } finally {
    captchaLoading.value = false
  }
}

function onTenantBlur() {
  const tenantCode = form.tenantCode?.trim()
  void refreshLoginBrand(false)
  if (tenantCode) {
    void refreshCaptcha(false)
  }
}

watch(
  () => form.tenantCode,
  () => {
    const code = form.tenantCode?.trim() || ''
    // 输入中只清本地验证码态，不请求后端
    if (code !== lastCaptchaTenant) {
      requireCaptcha.value = false
      captchaImage.value = ''
      form.captchaKey = ''
      form.captchaCode = ''
    }
    if (code !== lastBrandTenant) {
      // 清空租户时回退平台品牌（保留二维码/Logo），不要整页清空
      if (!code) {
        void refreshLoginBrand(true)
      }
    }
  },
)

function restoreRememberedLogin() {
  if (hasLoginPrefillQuery()) {
    return
  }
  const saved = loadLoginRemember()
  if (!saved) {
    return
  }
  form.tenantCode = saved.tenantCode
  form.username = saved.username
  form.password = saved.password
  form.rememberLogin = true
}

onMounted(() => {
  // 默认空白：不主动打 captcha。品牌始终拉平台包（二维码/Logo）；有租户时再按租户覆盖。
  if (hasLoginPrefillQuery()) {
    if (form.tenantCode.trim()) {
      void refreshLoginBrand(false)
      void refreshCaptcha(false)
    } else {
      void refreshLoginBrand(true)
    }
    return
  }
  restoreRememberedLogin()
  if (form.rememberLogin && form.tenantCode.trim()) {
    void refreshLoginBrand(false)
    void refreshCaptcha(false)
    return
  }
  // 对抗浏览器自动填充：退出后常把账号误填进租户码，会误触发「租户不存在」
  clearCredentialFields()
  form.rememberLogin = false
  void refreshLoginBrand(true)
  window.setTimeout(() => {
    if (!form.rememberLogin && !hasLoginPrefillQuery()) {
      clearCredentialFields()
      // 清自动填充后仍保留平台品牌素材
      void refreshLoginBrand(true)
    }
  }, 80)
})

function persistRememberPreference() {
  if (form.rememberLogin) {
    saveLoginRemember({
      tenantCode: form.tenantCode,
      username: form.username,
      password: form.password,
    })
  } else {
    clearLoginRemember()
  }
}

async function handleLogin() {
  if (mfaStep.value) {
    await handleMfaVerify()
    return
  }
  // 提交前按当前租户补一次探测（用户手动输入后可能未 blur）
  await refreshLoginBrand(false)
  await refreshCaptcha(false)
  await formRef.value?.validate()
  loading.value = true
  try {
    const payload = {
      tenantCode: form.tenantCode.trim(),
      username: form.username.trim(),
      password: form.password,
      ...(requireCaptcha.value
        ? { captchaKey: form.captchaKey, captchaCode: form.captchaCode }
        : {}),
    }
    const result = await userStore.login(payload)
    persistRememberPreference()
    if (result.mfaRequired && result.mfaTicket) {
      mfaStep.value = true
      mfaTicket.value = result.mfaTicket
      form.mfaCode = ''
      return
    }
    await afterLoginSuccess()
  } catch (err) {
    const raw = err instanceof Error ? err.message : ''
    if (raw.startsWith('error.')) {
      ElMessage.error(t(raw))
    }
    // 失败后策略可能变为必须验证码，强制刷新
    await refreshCaptcha(true)
  } finally {
    loading.value = false
  }
}

async function handleMfaVerify() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.completeMfa(mfaTicket.value, form.mfaCode)
    mfaStep.value = false
    mfaTicket.value = ''
    form.mfaCode = ''
    await afterLoginSuccess()
  } catch (err) {
    const raw = err instanceof Error ? err.message : ''
    if (raw.startsWith('error.')) {
      ElMessage.error(t(raw))
    }
    form.mfaCode = ''
  } finally {
    loading.value = false
  }
}

function cancelMfaStep() {
  mfaStep.value = false
  mfaTicket.value = ''
  form.mfaCode = ''
}

async function afterLoginSuccess() {
  if (userStore.user?.mustEnrollMfa) {
    await router.push('/force-enroll-mfa')
    return
  }
  await setupDynamicRoutes(router, { force: true })
  const productStore = useProductStore()
  await productStore.loadMine(true)
  await productStore.ensureSidebar()
  const home = productStore.current?.homePath?.trim()
  const landing =
    (home
      ? home.startsWith('/')
        ? home
        : `/${home}`
      : null) ||
    findFirstRegisteredPath(router, menuStore.allRouters) ||
    '/no-access'
  // 须改密时同样进入主布局，由 ForceChangePasswordGate 弹出改密
  router.push(landing)
}
</script>

<style scoped>
.login-page {
  --login-navy: var(--ic-sidebar-bg, #001529);
  --login-navy-end: #002140;
  --login-primary: var(--ic-primary, #1677ff);
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, var(--login-brand-fr)) minmax(380px, var(--login-form-fr));
  background: #fff;
}
.login-page--brand-right {
  grid-template-columns: minmax(380px, var(--login-form-fr)) minmax(0, var(--login-brand-fr));
}
.login-page--brand-right .login-aside {
  order: 2;
}
.login-page--brand-right .login-main {
  order: 1;
  border-left: 0;
  border-right: 1px solid #f0f0f0;
}

.login-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  border-left: 1px solid #f0f0f0;
  background: #fff;
}
.login-main__panel {
  width: min(360px, 100%);
}
.login-demo-banner {
  margin-bottom: 16px;
}
.login-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 28px;
}
.login-title {
  margin: 0;
  font-size: 26px;
  font-weight: 650;
  color: #141414;
}
.login-subtitle {
  margin: 8px 0 0;
  color: var(--ic-text-secondary, #8c8c8c);
  font-size: 14px;
}
.login-btn {
  width: 100%;
  margin-top: 8px;
  height: 42px;
  font-weight: 500;
}
.login-btn--ghost {
  margin-top: 10px;
}
.login-btn + .login-btn {
  margin-left: 0;
}
.login-remember-item {
  margin-bottom: 4px;
}
.login-remember-item :deep(.el-form-item__content) {
  line-height: 1.5;
}
.login-agree-item {
  margin-bottom: 8px;
}
.login-agree-item :deep(.el-form-item__content) {
  line-height: 1.5;
}
.login-agree {
  display: flex;
  align-items: flex-start;
}
.login-agree :deep(.el-checkbox) {
  height: auto;
  align-items: flex-start;
}
.login-agree :deep(.el-checkbox__label) {
  white-space: normal;
  line-height: 1.5;
  color: var(--el-text-color-regular);
}
.login-agree__text {
  font-size: 13px;
}
.login-agree__link {
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--login-primary);
  font: inherit;
  cursor: pointer;
}
.login-agree__link:hover {
  text-decoration: underline;
}
.login-agreement-body {
  max-height: min(60vh, 480px);
  overflow: auto;
  padding-right: 4px;
}
.login-agreement-version {
  margin: 0 0 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 600;
}
.login-agreement-intro {
  margin: 0 0 16px;
  color: var(--el-text-color-regular);
  line-height: 1.7;
  font-size: 13px;
}
.login-agreement-section {
  margin-bottom: 14px;
}
.login-agreement-section h3 {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 650;
  color: var(--el-text-color-primary);
}
.login-agreement-section p {
  margin: 0;
  color: var(--el-text-color-regular);
  line-height: 1.7;
  font-size: 13px;
}
.login-captcha {
  display: flex;
  gap: 10px;
  width: 100%;
  align-items: stretch;
}
.login-captcha :deep(.el-input) {
  flex: 1;
}
.login-captcha__image {
  width: 120px;
  height: 40px;
  padding: 0;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background: #f5f7fa;
  cursor: pointer;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}
.login-captcha__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.login-captcha__image:disabled {
  cursor: wait;
  opacity: 0.7;
}
.login-main__foot {
  margin: 20px 0 0;
  font-size: 12px;
  color: #8c8c8c;
}
.login-main__foot a {
  color: #595959;
  text-decoration: none;
}
.login-main__foot a:hover {
  color: var(--login-primary);
}

.login-aside {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 56px 56px 32px;
  color: rgba(255, 255, 255, 0.9);
  background:
    radial-gradient(ellipse 90% 60% at 85% 15%, rgba(22, 119, 255, 0.32), transparent 55%),
    linear-gradient(165deg, var(--login-navy) 0%, var(--login-navy-end) 55%, #001018 100%);
  overflow-x: hidden;
  overflow-y: auto;
  min-height: 100vh;
}
.login-aside::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.035) 1px, transparent 1px);
  background-size: 32px 32px;
  mask-image: linear-gradient(135deg, #000 20%, transparent 75%);
  pointer-events: none;
  opacity: 0.8;
}
.login-aside__glow {
  position: absolute;
  right: -20%;
  bottom: -10%;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(22, 119, 255, 0.22), transparent 68%);
  pointer-events: none;
}
.login-aside__inner,
.login-aside__footer {
  position: relative;
  z-index: 1;
}
.login-aside__logo {
  width: 64px;
  height: 64px;
  margin-bottom: 22px;
  border-radius: 14px;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.06);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.28);
}
.login-aside__title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: #fff;
  line-height: 1.25;
}
.login-aside__subtitle {
  margin: 10px 0 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.5;
}
.login-aside__intro {
  margin: 14px 0 0;
  max-width: 420px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.58);
  line-height: 1.7;
}
.login-aside__features {
  margin: 36px 0 0;
  padding: 0;
  max-width: 460px;
  list-style: none;
}
.login-aside__features li {
  position: relative;
  padding: 14px 0 14px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.login-aside__features li:last-child {
  border-bottom: 0;
}
.login-aside__features li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 20px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--login-primary);
}
.login-aside__features strong {
  display: block;
  margin-bottom: 4px;
  font-size: 15px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.92);
}
.login-aside__features span {
  display: block;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.55);
}
.login-aside__qr {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 28px;
}
.login-aside__qr-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  max-width: 112px;
  text-align: center;
  font-size: 12px;
  line-height: 1.4;
  color: rgba(255, 255, 255, 0.72);
}
.login-aside__qr-img {
  width: 96px;
  height: 96px;
  object-fit: contain;
  border-radius: 8px;
  background: #fff;
  padding: 6px;
  box-sizing: border-box;
}
.login-aside__dev-link {
  display: inline-block;
  margin-top: 28px;
  padding: 8px 14px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.88);
  text-decoration: none;
  background: rgba(255, 255, 255, 0.06);
  transition: background 0.15s ease, border-color 0.15s ease;
}
.login-aside__dev-link:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.4);
  color: #fff;
}
.login-aside__footer {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 40px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
}
.login-aside__brand {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}
.login-aside__footer a {
  color: rgba(255, 255, 255, 0.55);
  text-decoration: none;
}
.login-aside__footer a:hover {
  color: #fff;
}
.login-aside__dot {
  opacity: 0.45;
}

@media (max-width: 960px) {
  .login-page {
    grid-template-columns: 1fr;
  }
  .login-aside {
    min-height: auto;
    padding: 28px 24px 22px;
  }
  .login-aside__logo {
    width: 48px;
    height: 48px;
    margin-bottom: 14px;
    border-radius: 12px;
  }
  .login-aside__title {
    font-size: 24px;
  }
  .login-aside__features {
    margin-top: 20px;
  }
  .login-aside__features span {
    display: none;
  }
  .login-aside__qr {
    margin-top: 18px;
    gap: 16px;
  }
  .login-aside__qr-img {
    width: 80px;
    height: 80px;
  }
  .login-aside__dev-link {
    margin-top: 18px;
  }
  .login-aside__footer {
    margin-top: 20px;
  }
  .login-main {
    padding: 28px 20px 40px;
    border-left: 0;
  }
}
</style>
