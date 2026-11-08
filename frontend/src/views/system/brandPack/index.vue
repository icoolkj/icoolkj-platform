<!-- 站点品牌 OEM — view.json fileCatalogs + CatalogAttachmentManager（业务页不手写槽位循环） -->
<template>
  <div class="gen-page-shell">
    <GenListLayout
      v-loading="loading"
      :title="gp('title')"
      :description="gp('description')"
      :show-search="false"
      :show-right-toolbar="false"
    >
      <template #toolbar-extra>
        <el-button
          v-if="meta.tenantOverride && !isPlatformTarget"
          v-hasPermi="[PERM.update]"
          :disabled="saving"
          @click="handleReset"
        >
          {{ gp('resetOverride') }}
        </el-button>
        <el-button
          type="primary"
          v-hasPermi="[PERM.update]"
          :loading="saving"
          @click="handleSave"
        >
          {{ gp('save') }}
        </el-button>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        label-width="120px"
        class="brand-pack-form"
        @submit.prevent
      >
        <el-form-item v-if="meta.canSelectTenant" :label="gp('targetTenant')">
          <el-select
            v-model="targetTenantId"
            filterable
            style="width: 360px"
            @change="loadBrand"
          >
            <el-option :label="gp('platformTenant')" :value="PLATFORM_TENANT_ID" />
            <el-option
              v-for="item in tenantOptions"
              :key="item.id"
              :label="`${item.tenantName || item.tenantCode} (${item.tenantCode})`"
              :value="item.id!"
            />
          </el-select>
        </el-form-item>

        <el-alert
          v-if="!isPlatformTarget"
          :type="meta.tenantOverride ? 'warning' : 'success'"
          :closable="false"
          show-icon
          class="brand-pack-tip"
        >
          {{ meta.tenantOverride ? gp('tenantOverrideHint') : gp('tenantInheritHint') }}
        </el-alert>

        <el-tabs v-model="activeTab" class="brand-pack-tabs">
          <el-tab-pane :label="gp('sectionBasic')" name="basic">
            <el-form-item :label="gp('productNameZh')">
              <el-input v-model="form.productNameZh" maxlength="64" show-word-limit />
            </el-form-item>
            <el-form-item :label="gp('productNameEn')">
              <el-input v-model="form.productNameEn" maxlength="64" show-word-limit />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane
            v-for="section in catalogSections"
            :key="section.tab"
            :label="section.tabLabel"
            :name="section.tab"
          >
            <el-alert
              v-if="catalogLoadError"
              type="warning"
              :closable="false"
              show-icon
              class="brand-pack-tip"
            >
              {{ gp('catalogLoadHint') }}
            </el-alert>
            <template v-else>
              <CatalogAttachmentManager
                :ref="(el) => setCatalogRef(section.tab, el)"
                v-model="form.assets"
                :catalog-code="section.code"
                :download-scope="FILE_DOWNLOAD_SCOPE_INFRA"
                :force-variant="ATTACHMENT_VARIANT_IMAGE"
                :target-tenant-id="scopedTargetTenantId()"
                :gp="gp"
                @change="(p) => onCatalogChange(section.tab, p)"
              />
              <div v-if="section.enableLabels && labelSlots[section.tab]?.length" class="brand-pack-labels">
                <el-form-item
                  v-for="slot in labelSlots[section.tab]"
                  :key="slot.profileCode"
                  :label="slot.label"
                >
                  <el-input
                    v-model="form.labels[slot.profileCode]"
                    maxlength="64"
                    :placeholder="gp('assetLabelPlaceholder')"
                  />
                </el-form-item>
              </div>
            </template>
          </el-tab-pane>

          <el-tab-pane :label="gp('sectionCopyright')" name="copyright">
            <el-form-item :label="gp('copyrightBrand')">
              <el-input v-model="form.copyrightBrand" maxlength="64" />
            </el-form-item>
            <el-form-item :label="gp('icpNo')">
              <el-input v-model="form.icpNo" maxlength="64" />
            </el-form-item>
            <el-form-item :label="gp('icpUrl')">
              <el-input v-model="form.icpUrl" maxlength="256" />
            </el-form-item>
            <el-form-item :label="gp('domain')">
              <el-input v-model="form.domain" maxlength="128" />
            </el-form-item>
            <el-form-item :label="gp('siteUrl')">
              <el-input v-model="form.siteUrl" maxlength="256" />
            </el-form-item>
            <el-form-item :label="gp('email')">
              <el-input v-model="form.email" maxlength="128" />
            </el-form-item>
            <el-form-item :label="gp('contactName')">
              <el-input v-model="form.contactName" maxlength="64" />
            </el-form-item>
            <el-form-item :label="gp('contactPhone')">
              <el-input v-model="form.contactPhone" maxlength="32" />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import {
  ATTACHMENT_VARIANT_IMAGE,
  CatalogAttachmentManager,
  FILE_DOWNLOAD_SCOPE_INFRA,
  type CatalogSlot,
} from '@/components/attachment'
import { isPlatformTenant, PLATFORM_TENANT_ID } from '@/constants/flags'
import type { GenViewFileCatalogBind, GenViewSchema } from '@/types/tool/genView'
import {
  getBrandPack,
  updateBrandPack,
  resetBrandPack,
  PERM_BrandPack as PERM,
} from '@/api/system/brandPack'
import { pageTenant } from '@/api/tenant/tenant'
import type { TenantVO } from '@/types/tenant/tenant'
import { getConfigBootstrap } from '@/api/system/config'
import { useBrandStore } from '@/stores/brand'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { applyBrandChrome } from '@/utils/brandChrome'
import viewConfigData from '@/i18n/gen/brandPack/view.json'
const { gp } = useGenPageShellI18n('brandPack')
const brandStore = useBrandStore()
const viewConfig = viewConfigData as GenViewSchema

type CatalogSection = {
  tab: string
  code: string
  tabLabel: string
  enableLabels: boolean
}

type CatalogManagerExpose = {
  validate: () => boolean
  slots: { value: CatalogSlot[] }
}

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const activeTab = ref('basic')
const targetTenantId = ref(PLATFORM_TENANT_ID)
const tenantOptions = ref<TenantVO[]>([])
const catalogLoadError = ref(false)
const catalogSections = ref<CatalogSection[]>([])
const catalogRefs = ref<Record<string, CatalogManagerExpose>>({})
const labelSlots = reactive<Record<string, CatalogSlot[]>>({})
const meta = reactive({
  canSelectTenant: false,
  tenantOverride: false,
})

const form = reactive({
  productNameZh: '',
  productNameEn: '',
  copyrightBrand: '',
  icpNo: '',
  icpUrl: '',
  domain: '',
  siteUrl: '',
  email: '',
  contactName: '',
  contactPhone: '',
  /** CatalogAttachmentManager v-model；品牌包槽位均为单文件，保存时压成 string */
  assets: {} as Record<string, string | string[]>,
  labels: {} as Record<string, string>,
})

const isPlatformTarget = computed(() => isPlatformTenant(targetTenantId.value))

function scopedTargetTenantId(): string | undefined {
  return meta.canSelectTenant ? targetTenantId.value : undefined
}

function setCatalogRef(tab: string, el: unknown) {
  if (el) {
    catalogRefs.value[tab] = el as CatalogManagerExpose
  } else {
    delete catalogRefs.value[tab]
  }
}

function onCatalogChange(tab: string, payload: { slots: CatalogSlot[] }) {
  labelSlots[tab] = payload.slots || []
  if (!catalogSections.value.some((s) => s.tab === tab && s.enableLabels)) {
    return
  }
  if (!catalogSections.value.length) {
    catalogLoadError.value = false
  }
}

function flattenAssets(
  raw: Record<string, string | string[]>,
): Record<string, string> {
  const out: Record<string, string> = {}
  for (const [key, value] of Object.entries(raw || {})) {
    const id = Array.isArray(value) ? value[0] : value
    const trimmed = String(id || '').trim()
    if (trimmed) out[key] = trimmed
  }
  return out
}

async function loadTenants() {
  try {
    const res = await pageTenant({ pageNo: 1, pageSize: 200 })
    tenantOptions.value = (res.data.data?.list ?? []).filter(
      (item) => item.id && !isPlatformTenant(item.id),
    )
  } catch {
    tenantOptions.value = []
  }
}

function loadBrandCatalogBinds() {
  const binds: GenViewFileCatalogBind[] = viewConfig.fileCatalogs || []
  if (!binds.length) {
    catalogSections.value = []
    catalogLoadError.value = true
    return
  }
  catalogSections.value = binds.map((bind) => ({
    tab: bind.tab,
    code: bind.code,
    tabLabel: bind.tabLabelKey ? gp(bind.tabLabelKey) : bind.code,
    enableLabels: !!bind.enableLabels,
  }))
  catalogLoadError.value = false
}

async function loadBrand() {
  loading.value = true
  try {
    const res = await getBrandPack(scopedTargetTenantId())
    const data = res.data.data
    meta.canSelectTenant = !!data.canSelectTenant
    meta.tenantOverride = !!data.tenantOverride
    if (data.targetTenantId) {
      targetTenantId.value = data.targetTenantId
    }
    form.productNameZh = data.productNameI18n?.['zh-CN'] || data.productName || ''
    form.productNameEn = data.productNameI18n?.['en-US'] || ''
    form.copyrightBrand = data.copyrightBrand || ''
    form.icpNo = data.icpNo || ''
    form.icpUrl = data.icpUrl || ''
    form.domain = data.domain || ''
    form.siteUrl = data.siteUrl || ''
    form.email = data.email || ''
    form.contactName = data.contactName || ''
    form.contactPhone = data.contactPhone || ''
    form.assets = { ...(data.assets || {}) }
    form.labels = { ...(data.labels || {}) }
  } catch {
    ElMessage.error(gp('loadFailed'))
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  for (const manager of Object.values(catalogRefs.value)) {
    if (!manager?.validate?.()) {
      return
    }
  }
  saving.value = true
  try {
    const assets = flattenAssets(form.assets)
    const labels: Record<string, string> = {}
    for (const [k, v] of Object.entries(form.labels)) {
      if (v?.trim()) labels[k] = v.trim()
    }
    await updateBrandPack(
      {
        productName: form.productNameZh.trim() || form.productNameEn.trim(),
        productNameI18n: {
          'zh-CN': form.productNameZh.trim(),
          'en-US': form.productNameEn.trim(),
        },
        copyrightBrand: form.copyrightBrand.trim(),
        icpNo: form.icpNo.trim(),
        icpUrl: form.icpUrl.trim(),
        domain: form.domain.trim(),
        siteUrl: form.siteUrl.trim(),
        email: form.email.trim(),
        contactName: form.contactName.trim(),
        contactPhone: form.contactPhone.trim(),
        assets,
        labels,
      },
      scopedTargetTenantId(),
    )
    ElMessage.success(gp('saveSuccess'))
    await loadBrand()
    await refreshRuntimeBrand()
  } finally {
    saving.value = false
  }
}

async function handleReset() {
  await ElMessageBox.confirm(gp('resetConfirm'), { type: 'warning' })
  saving.value = true
  try {
    await resetBrandPack(scopedTargetTenantId())
    ElMessage.success(gp('resetSuccess'))
    await loadBrand()
    await refreshRuntimeBrand()
  } finally {
    saving.value = false
  }
}

async function refreshRuntimeBrand() {
  if (meta.canSelectTenant && !isPlatformTarget.value) {
    return
  }
  try {
    const res = await getConfigBootstrap()
    brandStore.setBrand(res.data.data?.brand)
    await applyBrandChrome()
  } catch {
    /* ignore */
  }
}

onMounted(async () => {
  loadBrandCatalogBinds()
  await loadBrand()
  if (meta.canSelectTenant) {
    await loadTenants()
  }
})
</script>

<style scoped>
.brand-pack-form {
  max-width: 960px;
}
.brand-pack-tip {
  margin-bottom: 12px;
}
.brand-pack-tabs {
  margin-top: 4px;
}
.brand-pack-tabs :deep(.el-tabs__content) {
  padding-top: 12px;
}
.brand-pack-labels {
  margin-top: 16px;
  max-width: 420px;
}
</style>
