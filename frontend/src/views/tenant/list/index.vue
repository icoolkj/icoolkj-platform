<!-- AUTO-GENERATED 租户 — GenRuntimePage + view.json -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="tenant"
    :api-override="apiOverride"
    :tree-sources="treeSources"
    :action-handlers="actionHandlers"
  />
  <TenantProvisionDialog v-model="provisionVisible" :info="provisionInfo" :gp="gp" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { GenRuntimePage } from '@/components/gen'
import type { GenCrudApi } from '@/types/gen/crudApi'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import type { TenantProvisionVO } from '@/types/tenant/tenant'
import {
  createTenant,
  disableTenant,
  enableTenant,
  pageTenant,
} from '@/api/tenant/tenant'
import { getRegionSelectableRoots } from '@/api/system/region'
import { TREE_ROOT_PARENT_ID } from '@/constants/flags'
import { buildGenTree } from '@/utils/genTree'
import { useGenPageShellI18n } from '@/composables/gen'
import TenantProvisionDialog from './TenantProvisionDialog.vue'

const { t } = useI18n()
const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
const { gp } = useGenPageShellI18n('tenant')

const regionRoots = ref<Record<string, unknown>[]>([])
const orgTenants = ref<Record<string, unknown>[]>([])
const treeSources = computed(() => ({
  regionRoots: regionRoots.value,
  orgTenants: orgTenants.value,
}))

const provisionVisible = ref(false)
const provisionInfo = ref<TenantProvisionVO | null>(null)

const apiOverride = computed<Partial<GenCrudApi>>(() => ({
  create: async (data) => {
    const res = await createTenant(data as Parameters<typeof createTenant>[0])
    provisionInfo.value = res.data.data ?? null
    provisionVisible.value = true
  },
  delete: async () => {},
}))

async function handleDisable(row: Record<string, unknown>) {
  if (!row.id) return
  await ElMessageBox.confirm(gp('disableConfirm'), t('common.tip'))
  await disableTenant(String(row.id))
  ElMessage.success(t('common.success'))
  await pageRef.value?.loadPage()
}

async function handleEnable(row: Record<string, unknown>) {
  if (!row.id) return
  await enableTenant(String(row.id))
  ElMessage.success(t('common.success'))
  await pageRef.value?.loadPage()
}

async function loadRegionRoots(tenantType?: string) {
  const res = await getRegionSelectableRoots(tenantType)
  const flat = (res.data.data ?? []).map((item) => ({
    ...item,
    label: `${item.regionName} (${item.regionCode})`,
  }))
  regionRoots.value = buildGenTree(
    flat as unknown as Record<string, unknown>[],
    'parentId',
    TREE_ROOT_PARENT_ID,
  )
}

async function loadOrgTenants() {
  const res = await pageTenant({ pageNo: 1, pageSize: 500, tenantType: 'ORGANIZATION' })
  const flat = (res.data.data?.list ?? []).map((item) => ({
    ...item,
    label: item.tenantName,
    parentId: item.parentTenantId || TREE_ROOT_PARENT_ID,
  }))
  orgTenants.value = buildGenTree(
    flat as unknown as Record<string, unknown>[],
    'parentId',
    TREE_ROOT_PARENT_ID,
  )
}

watch(
  () => pageRef.value?.form?.tenantType as string | undefined,
  (tenantType, prev) => {
    if (!tenantType) return
    void loadRegionRoots(tenantType)
    if (prev && prev !== tenantType) {
      const form = pageRef.value?.form
      if (form) {
        form.rootRegionId = undefined
        if (tenantType === 'ENTERPRISE') {
          form.parentTenantId = undefined
        }
      }
    }
  },
)

onMounted(async () => {
  await Promise.all([loadRegionRoots('ENTERPRISE'), loadOrgTenants()])
})

const actionHandlers: Record<string, GenRowActionHandler> = {
  'tenant.disable': async ({ row }) => handleDisable(row),
  'tenant.enable': async ({ row }) => handleEnable(row),
}
</script>

