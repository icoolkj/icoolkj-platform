<!-- 流程模型 — GenRuntimePage + 部署/版本/租户同步 -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="workflowModel"
    :action-handlers="actionHandlers"
    :row-action-visible="rowActionVisible"
  >
    <template v-if="!isPlatformTenant" #toolbar-extra>
      <el-button v-hasPermi="['workflow:model:update']" type="primary" plain @click="syncVisible = true">
        {{ gp('syncFromPlatform') }}
      </el-button>
    </template>
  </GenRuntimePage>

  <GenFormShell
    v-model="designVisible"
    :title="gp('designTitle')"
    :width="960"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <VerticalFlowDesigner
      v-if="designVisible && activeModelId"
      :model-id="activeModelId"
      :model-key="activeModelKey"
      @deployed="onDesignDeployed"
      @open-xml="openDeployFromDesign"
    />
    <template #footer>
      <el-button @click="designVisible = false">{{ t('common.cancel') }}</el-button>
    </template>
  </GenFormShell>

  <WorkflowModelDeployDialog
    v-model="deployVisible"
    :model-id="activeModelId"
    @success="pageRef?.loadPage()"
  />
  <WorkflowModelVersionDialog v-model="versionVisible" :model-id="activeModelId" />
  <WorkflowModelSyncDialog v-model="syncVisible" :gp="gp" @success="pageRef?.loadPage()" />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { GenRuntimePage } from '@/components/gen'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import type { GenViewRowAction } from '@/types/tool/genView'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import VerticalFlowDesigner from '@/views/workflow/designer/VerticalFlowDesigner.vue'
import WorkflowModelDeployDialog from '@/views/workflow/model/WorkflowModelDeployDialog.vue'
import WorkflowModelVersionDialog from '@/views/workflow/model/WorkflowModelVersionDialog.vue'
import WorkflowModelSyncDialog from '@/views/workflow/model/WorkflowModelSyncDialog.vue'
import { useGenPageShellI18n } from '@/composables/gen'
import { useUserStore } from '@/stores/user'
import { isPlatformTenant as matchPlatformTenant } from '@/constants/flags'

const { gp } = useGenPageShellI18n('workflowModel')
const { t } = useI18n()
const userStore = useUserStore()
const isPlatformTenant = computed(() => matchPlatformTenant(userStore.user?.tenantId))

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
const deployVisible = ref(false)
const versionVisible = ref(false)
const syncVisible = ref(false)
const designVisible = ref(false)
const activeModelId = ref<string>()
const activeModelKey = ref<string>()

function openDeploy(row: Record<string, unknown>) {
  activeModelId.value = row.id ? String(row.id) : undefined
  deployVisible.value = true
}

function openVersions(row: Record<string, unknown>) {
  activeModelId.value = row.id ? String(row.id) : undefined
  versionVisible.value = true
}

function openDesign(row: Record<string, unknown>) {
  activeModelId.value = row.id ? String(row.id) : undefined
  activeModelKey.value = row.modelKey ? String(row.modelKey) : undefined
  designVisible.value = true
}

function openDeployFromDesign() {
  deployVisible.value = true
}

function onDesignDeployed() {
  pageRef.value?.loadPage()
}

function rowActionVisible(action: GenViewRowAction, _row: Record<string, unknown>) {
  if (action.id === 'design' && isPlatformTenant.value) return false
  return undefined
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'workflowModel.design': async ({ row }) => openDesign(row),
  'workflowModel.deploy': async ({ row }) => openDeploy(row),
  'workflowModel.versions': async ({ row }) => openVersions(row),
}
</script>

