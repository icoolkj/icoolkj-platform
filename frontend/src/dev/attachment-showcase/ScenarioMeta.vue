<template>
  <div class="scenario-meta">
    <div class="scenario-meta__row">
      <el-tag size="small">{{ componentName }}</el-tag>
      <span class="scenario-meta__when">{{ when }}</span>
    </div>
    <pre class="scenario-meta__code"><code>{{ importHint }}</code></pre>
    <el-button size="small" text type="primary" @click="copy">{{ copyLabel }}</el-button>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

const props = defineProps<{
  componentName: string
  when: string
  importHint: string
  copyLabel: string
  copiedLabel: string
}>()

async function copy() {
  try {
    await navigator.clipboard.writeText(props.importHint)
    ElMessage.success(props.copiedLabel)
  } catch {
    ElMessage.warning(props.importHint)
  }
}
</script>

<style scoped lang="scss">
.scenario-meta {
  margin-bottom: 14px;
}

.scenario-meta__row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.scenario-meta__when {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.scenario-meta__code {
  margin: 0;
  padding: 12px 14px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.55;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
