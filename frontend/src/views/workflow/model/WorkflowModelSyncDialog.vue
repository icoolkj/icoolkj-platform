<!-- 同步平台流程模型目录 -->
<template>
  <el-dialog
    :model-value="modelValue"
    :title="gp('syncTitle')"
    width="720px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-alert type="info" :closable="false" show-icon class="mb-3" :title="gp('syncHint')" />
    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="bizType" :label="gp('syncBizType')" min-width="120" />
      <el-table-column prop="modelKey" :label="gp('syncModelKey')" min-width="120" />
      <el-table-column prop="modelName" :label="gp('syncModelName')" min-width="140" />
      <el-table-column prop="bindingMode" :label="gp('syncBindingMode')" width="140" />
      <el-table-column :label="gp('syncStatus')" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.synced ? 'success' : 'info'" size="small">
            {{ row.synced ? gp('syncSynced') : gp('syncPending') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.actions')" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :disabled="row.synced"
            v-hasPermi="[PERM_WorkflowModel.update]"
            @click="handleSync(row)"
          >
            {{ gp('syncAction') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  listWorkflowModelSyncCatalog,
  syncWorkflowModelFromPlatform,
  PERM_WorkflowModel,
} from '@/api/workflow/model'
import type { WorkflowModelSyncCatalogVO } from '@/types/workflow/model'

const props = defineProps<{
  modelValue: boolean
  gp: (key: string) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const { t } = useI18n()
const loading = ref(false)
const rows = ref<WorkflowModelSyncCatalogVO[]>([])

async function loadCatalog() {
  loading.value = true
  try {
    const res = await listWorkflowModelSyncCatalog()
    rows.value = res.data.data ?? []
  } finally {
    loading.value = false
  }
}

async function handleSync(row: WorkflowModelSyncCatalogVO) {
  if (!row.platformModelId || row.synced) return
  await syncWorkflowModelFromPlatform({ platformModelId: row.platformModelId })
  ElMessage.success(props.gp('syncSuccess'))
  emit('success')
  await loadCatalog()
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) void loadCatalog()
  },
)
</script>

<style scoped>
.mb-3 {
  margin-bottom: 12px;
}
</style>
