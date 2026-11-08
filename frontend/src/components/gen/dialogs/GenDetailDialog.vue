<!--
  标准详情 Dialog — 由 view.json detailDialog 驱动字段布局，api.get 拉数。
  行操作：{ "builtin": "detail" } 或 { "dialog": "GenDetailDialog" }。
-->
<template>
  <GenFormShell v-model="visible" :title="title || gp('detailTitle')" :width="dialogWidth">
    <div v-loading="loading">
      <el-descriptions
        v-if="detail"
        :column="column"
        border
        size="small"
        class="gen-detail-dialog__meta"
        :style="descriptionsStyle"
      >
        <el-descriptions-item
          v-for="item in fields"
          :key="item.field"
          :label="gf(item.field, 'label')"
          :span="item.span ?? 1"
        >
          <DictTag
            v-if="item.component === 'dict-tag' && item.dictType"
            :options="getDict(item.dictType)"
            :value="fieldValue(item.field)"
          />
          <pre v-else-if="item.component === 'json'" class="gen-detail-dialog__json">{{
            formatJson(fieldValue(item.field))
          }}</pre>
          <template v-else-if="item.component === 'datetime'">
            {{ formatDateTime(fieldValue(item.field)) }}
          </template>
          <template v-else-if="item.component === 'async-task-error'">
            {{ formatAsyncTaskError(fieldValue(item.field)) }}
          </template>
          <template v-else>{{ fieldValue(item.field) || '-' }}</template>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <template #footer>
      <el-button type="primary" @click="visible = false">{{ t('common.confirm') }}</el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import DictTag from '@/components/DictTag.vue'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { useDict } from '@/composables/useDict'
import { useGenI18n } from '@/composables/useGenI18n'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import type { GenViewDetailDialogConfig, GenViewSchema } from '@/types/tool/genView'
import { formatDateTime } from '@/utils/formatDateTime'
import { resolveAsyncTaskErrorMessage } from '@/utils/asyncTaskError'

defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue: boolean
  id?: string | null
  title?: string
  fileView: GenViewSchema
  businessName: string
  apiModule: string
  i18nFiles: Record<string, GenI18nMessages>
  get?: (id: string) => Promise<unknown>
}>()

const emit = defineEmits<{ 'update:modelValue': [boolean] }>()

const { t } = useI18n()
const { gp, gf } = useGenI18n(props.businessName, props.apiModule, props.i18nFiles)

const config = computed(() => (props.fileView.detailDialog ?? {}) as GenViewDetailDialogConfig)
const fields = computed(() => config.value.fields ?? [])
const dialogWidth = computed(() => config.value.dialogWidth ?? 860)
const labelWidth = computed(() => config.value.labelWidth ?? 120)
const column = computed(() => config.value.column ?? 2)

const descriptionsStyle = computed(() => ({
  '--gen-detail-label-width': `${labelWidth.value}px`,
}))

const dictTypes = computed(() => [
  ...new Set(fields.value.map((f) => f.dictType).filter((v): v is string => Boolean(v))),
])
const { getDict } = useDict(dictTypes)

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const loading = ref(false)
const detail = ref<Record<string, unknown> | null>(null)

function fieldValue(field: string): string | undefined {
  if (!detail.value) return undefined
  const value = detail.value[field]
  return value == null ? undefined : String(value)
}

function formatJson(raw?: string) {
  if (!raw?.trim()) return '-'
  try {
    return JSON.stringify(JSON.parse(raw), null, 2)
  } catch {
    return raw
  }
}

function formatAsyncTaskError(raw?: string) {
  if (!raw?.trim()) return '-'
  return resolveAsyncTaskErrorMessage(t, raw)
}

watch(
  () => [props.modelValue, props.id] as const,
  async ([open, id]) => {
    if (!open || !id) {
      detail.value = null
      return
    }
    if (!props.get) {
      console.warn('[GenDetailDialog] api.get is required')
      return
    }
    loading.value = true
    try {
      const res = await props.get(id)
      const data =
        res && typeof res === 'object' && 'data' in (res as object)
          ? (res as { data: { data?: Record<string, unknown> } }).data?.data
          : (res as Record<string, unknown>)
      detail.value = (data ?? null) as Record<string, unknown> | null
    } finally {
      loading.value = false
    }
  },
)
</script>

<style scoped>
.gen-detail-dialog__meta {
  margin-bottom: 4px;
}

.gen-detail-dialog__meta :deep(.el-descriptions__label) {
  width: var(--gen-detail-label-width);
  min-width: var(--gen-detail-label-width);
  box-sizing: border-box;
}

.gen-detail-dialog__json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
}
</style>
