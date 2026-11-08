<!-- OA 审批模板 — 列表；配置模板进入内页 -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="oaTenantTemplate"
    :view-override="viewOverride"
    :action-handlers="actionHandlers"
    :row-action-visible="rowActionVisible"
  >
    <template #toolbar-extra>
      <el-button v-hasPermi="['oa:tenantTemplate:enable']" type="primary" @click="enableVisible = true">
        {{ gp('enableAction') }}
      </el-button>
    </template>
  </GenRuntimePage>

  <OaTenantEnableDialog v-model="enableVisible" @success="onEnabled" />

  <OaTenantSyncPreviewDialog
    v-model="syncVisible"
    :template-id="syncTemplateId"
    @success="onSynced"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { GenRuntimePage } from '@/components/gen'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import type { GenViewRowAction } from '@/types/tool/genView'
import OaTenantEnableDialog from '@/views/oa/tenant-template/OaTenantEnableDialog.vue'
import OaTenantSyncPreviewDialog from '@/views/oa/tenant-template/OaTenantSyncPreviewDialog.vue'
import { useGenPageShellI18n } from '@/composables/gen'
import type { GenViewSchema } from '@/types/tool/genView'

const router = useRouter()

function viewOverride(view: GenViewSchema): GenViewSchema {
  return {
    ...view,
    table: {
      ...view.table,
      actions: { edit: false, delete: false, width: 380 },
    },
  }
}

const { gp } = useGenPageShellI18n('oaTenantTemplate')

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
const enableVisible = ref(false)
const syncVisible = ref(false)
const syncTemplateId = ref<string>()

function openSyncPreview(row: Record<string, unknown>) {
  if (!row.id) return
  syncTemplateId.value = String(row.id)
  syncVisible.value = true
}

function onSynced() {
  pageRef.value?.loadPage()
}

function openConfigure(row: Record<string, unknown>) {
  if (!row.id) return
  void router.push(`/oa/tenant-template/configure/${String(row.id)}`)
}

function hasPublished(row: Record<string, unknown>) {
  const verId = row.publishedTplVerId
  const verNo = Number(row.latestVersionNo ?? 0)
  return !!(verId || verNo > 0)
}

function openViewMapping(row: Record<string, unknown>) {
  if (!row.id) return
  void router.push({
    path: `/oa/tenant-template/configure/${String(row.id)}`,
    query: { mode: 'view' },
  })
}

function onEnabled() {
  pageRef.value?.loadPage()
}

function rowActionVisible(action: GenViewRowAction, row: Record<string, unknown>) {
  if (action.id === 'viewMapping') return hasPublished(row)
  return undefined
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'oaTenantTemplate.syncPreview': async ({ row }) => openSyncPreview(row),
  'oaTenantTemplate.viewMapping': async ({ row }) => openViewMapping(row),
  'oaTenantTemplate.configure': async ({ row }) => openConfigure(row),
}
</script>

