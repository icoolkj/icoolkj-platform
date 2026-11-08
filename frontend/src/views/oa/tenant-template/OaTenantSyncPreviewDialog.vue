<template>
  <el-dialog
    v-model="visible"
    :title="gp('syncPreviewTitle')"
    width="920px"
    destroy-on-close
    @closed="onClosed"
  >
    <div v-loading="loading" class="sync-preview">
      <template v-if="preview">
        <div class="summary">
          <el-space wrap>
            <el-tag type="info">
              {{ gp('platformLatest') }}: v{{ preview.platformLatestVersionNo ?? '-' }}
            </el-tag>
            <el-tag type="info">
              {{ gp('syncCurrentVersion') }}: v{{ preview.currentTenantVersionNo ?? '-' }}
              <span v-if="preview.currentTenantVersionStatus">
                · {{ preview.currentTenantVersionStatus }}
              </span>
            </el-tag>
            <el-tag type="success">
              {{ gp('syncAddedCount') }}: {{ preview.addedCount ?? 0 }}
            </el-tag>
            <el-tag type="danger">
              {{ gp('syncRemovedCount') }}: {{ preview.removedCount ?? 0 }}
            </el-tag>
            <el-tag type="warning">
              {{ gp('syncChangedCount') }}: {{ preview.changedCount ?? 0 }}
            </el-tag>
            <el-tag>{{ gp('syncKeptCustomCount') }}: {{ preview.keptCustomCount ?? 0 }}</el-tag>
          </el-space>
          <p class="tip">{{ gp('syncPreviewTip') }}</p>
        </div>

        <el-empty v-if="!preview.hasChanges" :description="gp('syncNoChanges')" />
        <el-table v-else :data="preview.diffs" border size="small" max-height="420">
          <el-table-column
            prop="changeType"
            :label="gp('syncChangeType')"
            width="110"
            align="center"
          >
            <template #default="{ row }">
              <el-tag size="small" :type="changeTagType(row.changeType)">
                {{ changeLabel(row.changeType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="fieldCode" :label="gp('syncFieldCode')" width="130" />
          <el-table-column prop="fieldName" :label="gp('syncFieldName')" width="120" />
          <el-table-column
            prop="currentSummary"
            :label="gp('syncCurrentSummary')"
            min-width="220"
            show-overflow-tooltip
          />
          <el-table-column
            prop="afterSummary"
            :label="gp('syncAfterSummary')"
            min-width="220"
            show-overflow-tooltip
          />
        </el-table>
      </template>
    </div>
    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button
        type="primary"
        :loading="confirming"
        :disabled="!preview?.hasChanges"
        @click="confirmSync"
      >
        {{ gp('syncConfirmAction') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  previewOaTenantSyncFromPlatform,
  syncOaTenantTemplateFromPlatform,
} from '@/api/oa/oaTenantTemplate'
import type { OaTenantSyncPreviewVO } from '@/types/oa/oaTenantTemplate'

const props = defineProps<{
  modelValue: boolean
  templateId?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const { gp } = useGenPageShellI18n('oaTenantTemplate')
const { t } = useI18n()

const loading = ref(false)
const confirming = ref(false)
const preview = ref<OaTenantSyncPreviewVO>()

const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit('update:modelValue', v),
})

watch(
  () => [props.modelValue, props.templateId] as const,
  async ([open, id]) => {
    if (!open || !id) return
    loading.value = true
    preview.value = undefined
    try {
      const res = await previewOaTenantSyncFromPlatform(id)
      preview.value = res.data.data
    } finally {
      loading.value = false
    }
  },
)

function changeTagType(type?: string) {
  if (type === 'ADDED') return 'success'
  if (type === 'REMOVED') return 'danger'
  if (type === 'CHANGED') return 'warning'
  return 'info'
}

function changeLabel(type?: string) {
  if (type === 'ADDED') return gp('syncChangeAdded')
  if (type === 'REMOVED') return gp('syncChangeRemoved')
  if (type === 'CHANGED') return gp('syncChangeChanged')
  if (type === 'KEPT_CUSTOM') return gp('syncChangeKeptCustom')
  return type || '-'
}

async function confirmSync() {
  if (!props.templateId || !preview.value?.hasChanges) return
  confirming.value = true
  try {
    await syncOaTenantTemplateFromPlatform(props.templateId)
    ElMessage.success(gp('syncedPlatform'))
    visible.value = false
    emit('success')
  } finally {
    confirming.value = false
  }
}

function onClosed() {
  preview.value = undefined
}
</script>

<style scoped>
.sync-preview .summary {
  margin-bottom: 12px;
}
.sync-preview .tip {
  margin: 10px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.5;
}
</style>
