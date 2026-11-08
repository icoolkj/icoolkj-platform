<template>
  <el-dialog
    :model-value="modelValue"
    :title="title ?? gp('saveDiffTitle')"
    width="88%"
    top="4vh"
    destroy-on-close
    class="gen-json-diff-dialog"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-if="unchanged" class="unchanged-tip">{{ gp('noChanges') }}</div>
    <div v-else class="diff-grid">
      <div class="diff-pane">
        <div class="diff-label">{{ gp('diffBefore') }}</div>
        <pre class="diff-pre"><code><div
          v-for="(row, idx) in diffRows"
          :key="'b-' + idx"
          class="diff-line"
          :class="{ 'is-changed': row.changed }"
        ><span v-if="row.before">{{ row.before }}</span><span v-else>&nbsp;</span></div></code></pre>
      </div>
      <div class="diff-pane">
        <div class="diff-label">{{ gp('diffAfter') }}</div>
        <pre class="diff-pre"><code><div
          v-for="(row, idx) in diffRows"
          :key="'a-' + idx"
          class="diff-line"
          :class="{ 'is-changed': row.changed }"
        ><span v-if="row.after">{{ row.after }}</span><span v-else>&nbsp;</span></div></code></pre>
      </div>
    </div>
    <template #footer>
      <el-button @click="handleCancel">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :disabled="unchanged" @click="handleConfirm">
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToolGenI18n } from '@/composables/useToolGenI18n'
import { buildJsonDiffRows, jsonTextEqual } from '@/utils/jsonDiff'

const props = defineProps<{
  modelValue: boolean
  beforeText: string
  afterText: string
  title?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  confirm: []
  cancel: []
}>()

const { t } = useI18n()
const { gp } = useToolGenI18n()

const unchanged = computed(() => jsonTextEqual(props.beforeText, props.afterText))
const diffRows = computed(() => buildJsonDiffRows(props.beforeText, props.afterText))

function handleCancel() {
  emit('update:modelValue', false)
  emit('cancel')
}

function handleConfirm() {
  emit('update:modelValue', false)
  emit('confirm')
}
</script>

<style scoped>
.unchanged-tip {
  padding: 24px;
  text-align: center;
  color: var(--el-text-color-secondary);
}
.diff-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.diff-label {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
}
.diff-pre {
  margin: 0;
  max-height: 62vh;
  overflow: auto;
  padding: 12px;
  background: #fafafa;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  line-height: 1.5;
}
.diff-line {
  white-space: pre;
}
.diff-line.is-changed {
  background: #fff7e6;
}
</style>
