<template>
  <div class="gen-i18n-panel">
    <el-row :gutter="12" class="toolbar">
      <el-col :span="8">
        <el-select v-model="activeLocale" style="width: 100%">
          <el-option label="zh-CN" value="zh-CN" />
          <el-option label="en-US" value="en-US" />
        </el-select>
      </el-col>
      <el-col :span="16" class="toolbar-actions">
        <el-radio-group v-model="editMode" size="small" @change="onModeChange">
          <el-radio-button value="form">{{ gp('i18nModeForm') }}</el-radio-button>
          <el-radio-button value="json">{{ gp('i18nModeJson') }}</el-radio-button>
        </el-radio-group>
        <el-button @click="handleRestoreBaseline">{{ gp('restoreGitBaseline') }}</el-button>
        <el-button @click="handleSync">{{ gp('i18nSync') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ t('common.save') }}
        </el-button>
      </el-col>
    </el-row>

    <template v-if="editMode === 'form'">
      <el-empty v-if="!hasFieldData" :description="gp('i18nEmpty')" />

      <template v-else>
        <div class="section-title">{{ gp('i18nPageTexts') }}</div>
        <el-form label-width="120px" class="page-form">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item :label="gp('i18nPageTitle')">
                <el-input v-model="page.title" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="gp('i18nAddDialogTitle')">
                <el-input v-model="page.addTitle" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="gp('i18nEditDialogTitle')">
                <el-input v-model="page.editTitle" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="gp('i18nDeleteConfirmText')">
                <el-input v-model="page.deleteConfirm" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <div class="section-title">{{ gp('i18nFieldTexts') }}</div>
        <el-table :data="fieldRows" row-key="javaField" max-height="420" stripe border>
          <el-table-column
            :label="gp('javaField')"
            prop="javaField"
            width="130"
            show-overflow-tooltip
          />
          <el-table-column
            :label="gp('columnComment')"
            prop="columnComment"
            width="140"
            show-overflow-tooltip
          />
          <el-table-column :label="gp('i18nDisplayLabel')" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.field.label" />
            </template>
          </el-table-column>
          <el-table-column :label="gp('i18nPlaceholder')" min-width="140">
            <template #default="{ row }">
              <el-input
                v-model="row.field.placeholder"
                :placeholder="row.hasPlaceholder ? undefined : '—'"
                :disabled="!row.hasPlaceholder"
              />
            </template>
          </el-table-column>
          <el-table-column :label="gp('i18nValRequired')" min-width="160">
            <template #default="{ row }">
              <el-input v-if="row.constraint?.required" v-model="row.validation.required" />
              <span v-else class="cell-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column :label="gp('i18nValMaxLength')" min-width="160">
            <template #default="{ row }">
              <el-input v-if="row.constraint?.maxLength" v-model="row.validation.maxLength" />
              <span v-else class="cell-muted">—</span>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </template>

    <el-input
      v-else
      v-model="jsonText"
      type="textarea"
      :rows="22"
      spellcheck="false"
      class="json-editor"
      :placeholder="gp('i18nJsonPlaceholder')"
    />
    <GenJsonDiffDialog
      v-model="diffVisible"
      :before-text="savedJsonText"
      :after-text="pendingJsonText"
      @confirm="commitSave"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToolGenI18n } from '@/composables/useToolGenI18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clearGenI18nCache } from '@/composables/useGenI18n'
import { getGenI18n, restoreGenI18nBaseline, saveGenI18n, syncGenI18n } from '@/api/tool/genI18n'
import GenJsonDiffDialog from './GenJsonDiffDialog.vue'
import { formatJsonText, jsonTextEqual } from '@/utils/jsonDiff'
import type { GenTableColumn } from '@/types/tool/gen'
import type { GenFieldConstraints, GenFieldMessages, GenI18nMessages } from '@/types/tool/genI18n'

const props = defineProps<{
  tableId: string
  businessName: string
  apiModule: string
  columns?: GenTableColumn[]
}>()

const { t } = useI18n()
const { gp } = useToolGenI18n()

const activeLocale = ref('zh-CN')
const editMode = ref<'form' | 'json'>('form')
const jsonText = ref('')
const savedJsonText = ref('')
const pendingJsonText = ref('')
const diffVisible = ref(false)
const rowId = ref<string>()
const saving = ref(false)
const messages = ref<GenI18nMessages | null>(null)

const page = reactive({
  title: '',
  addTitle: '',
  editTitle: '',
  deleteConfirm: '',
})

watch(
  page,
  () => {
    if (!messages.value) return
    messages.value.page = { ...page }
  },
  { deep: true },
)

const hasFieldData = computed(() => {
  const fields = messages.value?.fields
  return fields != null && Object.keys(fields).length > 0
})

interface FieldRow {
  javaField: string
  columnComment?: string
  field: GenFieldMessages
  validation: NonNullable<GenFieldMessages['validation']>
  constraint?: GenFieldConstraints
  hasPlaceholder: boolean
}

const fieldRows = computed<FieldRow[]>(() => {
  const fields = messages.value?.fields ?? {}
  const constraints = messages.value?.meta?.constraints ?? {}
  const orderedKeys: string[] = []

  if (props.columns?.length) {
    for (const col of props.columns) {
      if (col.javaField && fields[col.javaField]) {
        orderedKeys.push(col.javaField)
      }
    }
  }
  for (const key of Object.keys(fields)) {
    if (!orderedKeys.includes(key)) {
      orderedKeys.push(key)
    }
  }

  const commentMap = new Map(
    (props.columns ?? [])
      .filter((c) => c.javaField)
      .map((c) => [c.javaField!, c.columnComment ?? '']),
  )

  return orderedKeys.map((javaField) => {
    const field = fields[javaField]!
    return {
      javaField,
      columnComment: commentMap.get(javaField),
      field,
      validation: field.validation!,
      constraint: constraints[javaField],
      hasPlaceholder: field.placeholder !== undefined,
    }
  })
})

function normalizeMessages(data: GenI18nMessages): GenI18nMessages {
  if (data.fields) {
    for (const field of Object.values(data.fields)) {
      if (!field.validation) {
        field.validation = {}
      }
    }
  }
  return data
}

function syncPageFromMessages(data: GenI18nMessages | null) {
  page.title = data?.page?.title ?? ''
  page.addTitle = data?.page?.addTitle ?? ''
  page.editTitle = data?.page?.editTitle ?? ''
  page.deleteConfirm = data?.page?.deleteConfirm ?? ''
}

function applyLoadedJson(raw?: string | null) {
  if (!raw?.trim()) {
    messages.value = null
    jsonText.value = ''
    savedJsonText.value = ''
    syncPageFromMessages(null)
    return
  }
  try {
    const parsed = normalizeMessages(JSON.parse(raw) as GenI18nMessages)
    messages.value = parsed
    jsonText.value = JSON.stringify(parsed, null, 2)
    savedJsonText.value = jsonText.value
    syncPageFromMessages(parsed)
  } catch {
    messages.value = null
    jsonText.value = raw
    savedJsonText.value = raw
    editMode.value = 'json'
    syncPageFromMessages(null)
  }
}

function buildJsonPayload(): string {
  if (editMode.value === 'json') {
    return jsonText.value
  }
  return JSON.stringify(messages.value ?? {}, null, 2)
}

function onModeChange(mode: string | number | boolean | undefined) {
  if (mode === 'json') {
    if (messages.value) {
      jsonText.value = JSON.stringify(messages.value, null, 2)
    }
    return
  }
  if (!jsonText.value.trim()) {
    messages.value = null
    syncPageFromMessages(null)
    return
  }
  try {
    const parsed = normalizeMessages(JSON.parse(jsonText.value) as GenI18nMessages)
    messages.value = parsed
    syncPageFromMessages(parsed)
  } catch {
    ElMessage.error(gp('i18nJsonInvalid'))
    editMode.value = 'json'
  }
}

async function loadLocale() {
  if (!props.tableId) return
  const res = await getGenI18n(props.tableId, activeLocale.value)
  const row = res.data.data
  rowId.value = row?.id
  applyLoadedJson(row?.messagesJson)
  if (hasFieldData.value) {
    editMode.value = 'form'
  }
}

async function handleSync() {
  await syncGenI18n(props.tableId)
  clearGenI18nCache(props.businessName, props.apiModule)
  ElMessage.success(gp('i18nSyncSuccess'))
  await loadLocale()
}

async function handleRestoreBaseline() {
  await ElMessageBox.confirm(gp('restoreGitBaselineConfirm'), t('common.tip'), {
    type: 'warning',
  })
  try {
    await restoreGenI18nBaseline(props.tableId, activeLocale.value)
    clearGenI18nCache(props.businessName, props.apiModule)
    ElMessage.success(gp('restoreGitBaselineSuccess'))
    await loadLocale()
  } catch {
    // API error already surfaced
  }
}

async function commitSave() {
  saving.value = true
  try {
    const payload = pendingJsonText.value
    await saveGenI18n({
      id: rowId.value,
      tableId: props.tableId,
      locale: activeLocale.value,
      messagesJson: payload,
    })
    clearGenI18nCache(props.businessName, props.apiModule)
    ElMessage.success(t('common.success'))
    await loadLocale()
  } finally {
    saving.value = false
  }
}

async function handleSave() {
  try {
    const payload = buildJsonPayload()
    JSON.parse(payload)
    const formatted = formatJsonText(payload)
    if (jsonTextEqual(savedJsonText.value, formatted)) {
      ElMessage.info(gp('noChanges'))
      return
    }
    pendingJsonText.value = formatted
    diffVisible.value = true
  } catch {
    ElMessage.error(gp('i18nJsonInvalid'))
  }
}

watch(() => props.tableId, loadLocale, { immediate: true })
watch(activeLocale, loadLocale)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.toolbar-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  margin: 8px 0 12px;
  color: var(--el-text-color-primary);
}
.page-form {
  margin-bottom: 16px;
}
.cell-muted {
  color: var(--el-text-color-placeholder);
}
.json-editor :deep(textarea) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
}
</style>
