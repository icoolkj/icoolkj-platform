<template>
  <div class="gen-view-panel">
    <div class="toolbar">
      <el-button @click="handleRestoreBaseline">{{ gp('restoreGitBaseline') }}</el-button>
      <el-button @click="handleSync">{{ gp('viewSync') }}</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">
        {{ t('common.save') }}
      </el-button>
    </div>
    <el-input
      v-model="jsonText"
      type="textarea"
      :rows="24"
      spellcheck="false"
      class="json-editor"
      :placeholder="gp('viewJsonPlaceholder')"
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
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToolGenI18n } from '@/composables/useToolGenI18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clearGenViewCache } from '@/composables/useGenView'
import { getGenView, restoreGenViewBaseline, saveGenView, syncGenView } from '@/api/tool/genView'
import GenJsonDiffDialog from './GenJsonDiffDialog.vue'
import { formatJsonText, jsonTextEqual } from '@/utils/jsonDiff'

const props = defineProps<{
  tableId: string
  businessName: string
  apiModule: string
}>()

const { t } = useI18n()
const { gp } = useToolGenI18n()

const jsonText = ref('')
const savedJsonText = ref('')
const pendingJsonText = ref('')
const diffVisible = ref(false)
const rowId = ref<string>()
const saving = ref(false)

function applyJson(raw?: string | null) {
  if (!raw?.trim()) {
    jsonText.value = ''
    savedJsonText.value = ''
    return
  }
  try {
    jsonText.value = formatJsonText(raw)
    savedJsonText.value = jsonText.value
  } catch {
    jsonText.value = raw
    savedJsonText.value = raw
  }
}

async function loadView() {
  if (!props.tableId) return
  const res = await getGenView(props.tableId)
  const row = res.data.data
  rowId.value = row?.id
  applyJson(row?.viewJson)
}

async function handleRestoreBaseline() {
  await ElMessageBox.confirm(gp('restoreGitBaselineConfirm'), t('common.tip'), {
    type: 'warning',
  })
  try {
    await restoreGenViewBaseline(props.tableId)
    clearGenViewCache(props.businessName, props.apiModule)
    ElMessage.success(gp('restoreGitBaselineSuccess'))
    await loadView()
  } catch {
    // API error already surfaced
  }
}

async function handleSync() {
  await syncGenView(props.tableId)
  clearGenViewCache(props.businessName, props.apiModule)
  ElMessage.success(gp('viewSyncSuccess'))
  await loadView()
}

async function commitSave() {
  saving.value = true
  try {
    await saveGenView({
      id: rowId.value,
      tableId: props.tableId,
      viewJson: pendingJsonText.value,
    })
    clearGenViewCache(props.businessName, props.apiModule)
    ElMessage.success(t('common.success'))
    await loadView()
  } finally {
    saving.value = false
  }
}

async function handleSave() {
  try {
    JSON.parse(jsonText.value)
    const formatted = formatJsonText(jsonText.value)
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

watch(() => props.tableId, loadView, { immediate: true })
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.json-editor :deep(textarea) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
}
</style>
