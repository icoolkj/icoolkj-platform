<template>
  <div class="ai-log-text-block">
    <div v-if="label" class="block-label">{{ label }}</div>
    <el-input
      :model-value="text || ''"
      type="textarea"
      :rows="rows"
      readonly
      class="mono-textarea"
      spellcheck="false"
    />
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    label?: string
    text?: string | null
    rows?: number
  }>(),
  { rows: 12 },
)
</script>

<style scoped lang="scss">
@use '@/styles/tool-ai.scss';

.block-label {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
}
</style>
