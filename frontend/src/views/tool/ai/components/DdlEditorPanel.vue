<template>
  <div style="margin-top: 16px">
    <div class="toolbar-row">
      <span class="field-label">{{ gp('ddlEditor') }}</span>
      <el-button :disabled="!modelValue.trim()" @click="copyDdl">
        {{ gp('copyDdl') }}
      </el-button>
    </div>
    <p class="form-tip">{{ hint }}</p>
    <p class="form-tip">{{ gp('ddlManualExecuteHint') }}</p>
    <el-input
      :model-value="modelValue"
      type="textarea"
      :rows="rows"
      :placeholder="placeholder"
      class="mono-textarea"
      spellcheck="false"
      @update:model-value="emit('update:modelValue', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { useToolAiI18n } from '@/composables/useToolAiI18n'
import { markAiDdlCopied } from '@/api/tool/aiLog'

const props = withDefaults(
  defineProps<{
    modelValue: string
    hint: string
    placeholder?: string
    rows?: number
    ddlRecordId?: string
  }>(),
  { rows: 18 },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'copied'): void
}>()

const { gp } = useToolAiI18n()

async function copyDdl() {
  const text = props.modelValue.trim()
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    if (props.ddlRecordId) {
      await markAiDdlCopied(props.ddlRecordId, { finalDdl: text })
    }
    ElMessage.success(props.ddlRecordId ? gp('markCopied') : gp('copyDdlSuccess'))
    emit('copied')
  } catch {
    ElMessage.error(gp('copyDdlFailed'))
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/tool-ai.scss';

.field-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
</style>
