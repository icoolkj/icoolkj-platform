<!-- 数据库备份 — GenListLayout + 嵌套 GenRuntimePage（extensions 行操作） -->
<template>
  <div class="gen-page-shell">
    <GenListLayout
      :title="gp('title')"
      :description="gp('description')"
      :show-search="false"
      :show-right-toolbar="false"
      class="database-backup-page"
    >
      <template #table-top>
        <el-tabs v-model="activeTab" class="backup-tabs" @tab-change="handleTabChange">
          <el-tab-pane :label="gp('tabFiles')" name="files" />
          <el-tab-pane :label="gp('tabPolicy')" name="policy" />
        </el-tabs>
      </template>

      <div v-show="activeTab === 'files'" class="backup-files-host">
        <GenRuntimePage
          ref="pageRef"
          page-key="databaseBackup"
          hide-header
          read-only
          :action-handlers="actionHandlers"
          :row-action-visible="rowActionVisible"
        >
          <template #toolbar-extra>
            <el-button
              v-if="!demoStore.isDemo"
              type="primary"
              :loading="running"
              v-hasPermi="['monitor:dbBackup:run']"
              @click="handleRun"
            >
              {{ gp('runNow') }}
            </el-button>
          </template>
        </GenRuntimePage>
      </div>

      <div v-show="activeTab === 'policy'" class="backup-policy-host">
        <DatabaseBackupPolicyPanel ref="policyPanelRef" />
      </div>
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import { GenRuntimePage } from '@/components/gen'
import { useGenPageShellI18n } from '@/composables/gen'
import DatabaseBackupPolicyPanel from './DatabaseBackupPolicyPanel.vue'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import type { GenViewRowAction } from '@/types/tool/genView'
import {
  deleteDatabaseBackup,
  runDatabaseBackup,
} from '@/api/infra/databaseBackup'
import { downloadByApi } from '@/utils/download'
import { useDemoStore } from '@/stores/demo'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const demoStore = useDemoStore()
const { gp } = useGenPageShellI18n('databaseBackup')

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
const policyPanelRef = ref<InstanceType<typeof DatabaseBackupPolicyPanel>>()

const activeTab = ref(route.query.tab === 'policy' ? 'policy' : 'files')
const running = ref(false)

function rowActionVisible(action: GenViewRowAction, _row: Record<string, unknown>) {
  if (action.id === 'delete' && demoStore.isDemo) return false
  return undefined
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'databaseBackup.download': async ({ row }) => {
    const fileName = String(row.fileName ?? row.id ?? '')
    if (!fileName) return
    try {
      await downloadByApi(`/infra/databaseBackup/${encodeURIComponent(fileName)}/download`, fileName)
    } catch (err) {
      ElMessage.error(err instanceof Error ? err.message : t('common.downloadFailed'))
    }
  },
  'databaseBackup.delete': async ({ row, reload }) => {
    const fileName = String(row.fileName ?? row.id ?? '')
    if (!fileName) return
    await ElMessageBox.confirm(gp('deleteConfirm', { name: fileName }), t('common.tip'))
    await deleteDatabaseBackup(fileName)
    ElMessage.success(t('common.success'))
    await reload()
  },
}

watch(activeTab, (tab) => {
  const query = { ...route.query }
  if (tab === 'policy') {
    query.tab = 'policy'
  } else {
    delete query.tab
  }
  router.replace({ query })
})

function handleTabChange(name: string | number) {
  if (name === 'policy') {
    policyPanelRef.value?.loadConfig()
  }
}

async function handleRun() {
  await ElMessageBox.confirm(gp('runConfirm'), t('common.tip'))
  running.value = true
  try {
    const res = await runDatabaseBackup()
    ElMessage.success(gp('runSuccess', { name: res.data.data?.fileName ?? '' }))
    await pageRef.value?.loadPage()
  } finally {
    running.value = false
  }
}
</script>

<style scoped>
.backup-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.backup-files-host :deep(.page-container) {
  padding: 0;
}
.backup-files-host :deep(.search-card) {
  margin-bottom: 12px;
  border: none;
  box-shadow: none;
  background: transparent;
}
.backup-files-host :deep(.search-card .el-card__body) {
  padding: 0 0 8px;
}
.backup-files-host :deep(.table-card) {
  border: none;
  box-shadow: none;
  background: transparent;
}
.backup-files-host :deep(.table-card > .el-card__body) {
  padding: 0;
}

.backup-policy-host {
  padding-top: 4px;
}
</style>

