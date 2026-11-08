<!-- 定时任务 — GenRuntimePage + view.json + 立即执行/启停 -->
<template>
  <GenRuntimePage
    page-key="managedJob"
    :api-override="apiOverride"
    :action-handlers="actionHandlers"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { GenRuntimePage } from '@/components/gen'
import type { GenCrudApi } from '@/types/gen/crudApi'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import {
  changeJobStatus,
  pageJobs,
  runJob,
  type SysJobVO,
} from '@/api/monitor/job'
import { loadGenPageBundle } from '@/composables/gen'
import type { GenI18nMessages } from '@/types/tool/genI18n'

const { locale } = useI18n()
let GEN_I18N_FILES: Record<string, GenI18nMessages> = {}

function localizeJobName(row: SysJobVO): SysJobVO {
  const keys = GEN_I18N_FILES[locale.value]?.keys ?? GEN_I18N_FILES['zh-CN']?.keys ?? {}
  const localized = keys[row.jobCode]
  return localized ? { ...row, jobName: localized } : row
}

async function pageManagedJob(params: Record<string, unknown>) {
  const bundle = await loadGenPageBundle('managedJob')
  GEN_I18N_FILES = bundle?.i18nFiles ?? {}
  const res = await pageJobs(params)
  const page = res.data.data
  const list = (page.list ?? []).map(localizeJobName)
  return {
    ...res,
    data: {
      ...res.data,
      data: { ...page, list },
    },
  }
}

const apiOverride = computed<Partial<GenCrudApi>>(() => ({
  page: pageManagedJob,
}))

function displayName(row: Record<string, unknown>) {
  const code = String(row.jobCode ?? '')
  const keys = GEN_I18N_FILES[locale.value]?.keys ?? GEN_I18N_FILES['zh-CN']?.keys ?? {}
  return keys[code] ?? String(row.jobName ?? code)
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'managedJob.run': async ({ row, gp: rowGp, t, reload }) => {
    const code = String(row.jobCode ?? '')
    if (!code) return
    await ElMessageBox.confirm(rowGp('runConfirm', { name: displayName(row) }), t('common.tip'))
    await runJob(code)
    ElMessage.success(t('common.success'))
    await reload()
  },
  'managedJob.pause': async ({ row, gp: rowGp, t, reload }) => {
    const id = String(row.id ?? '')
    if (!id) return
    await ElMessageBox.confirm(rowGp('pauseConfirm', { name: displayName(row) }), t('common.tip'))
    await changeJobStatus(id, '1')
    ElMessage.success(t('common.success'))
    await reload()
  },
  'managedJob.resume': async ({ row, gp: rowGp, t, reload }) => {
    const id = String(row.id ?? '')
    if (!id) return
    await ElMessageBox.confirm(rowGp('resumeConfirm', { name: displayName(row) }), t('common.tip'))
    await changeJobStatus(id, '0')
    ElMessage.success(t('common.success'))
    await reload()
  },
}
</script>

<script lang="ts">
export default { name: 'SystemManagedJob' }
</script>

