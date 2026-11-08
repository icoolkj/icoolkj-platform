<template>
  <div class="state-dump">
    <div class="state-dump__label">{{ label }}</div>
    <pre>{{ pretty }}</pre>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label: string
  value: unknown
}>()

const pretty = computed(() => JSON.stringify(props.value, null, 2))
</script>

<style scoped lang="scss">
.state-dump {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed var(--el-border-color);
}

.state-dump__label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

pre {
  margin: 0;
  padding: 10px 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  overflow: auto;
}
</style>
