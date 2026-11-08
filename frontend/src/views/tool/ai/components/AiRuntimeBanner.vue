<template>
  <el-alert
    v-if="status && aiReady"
    type="success"
    :closable="false"
    show-icon
    class="ai-runtime-banner"
  >
    <template #title>
      <span class="ai-runtime-banner__title">{{ titleText }}</span>
    </template>
    <div class="ai-runtime-banner__meta">
      <span v-if="status.providerName">{{ status.providerName }}</span>
      <span v-if="modelLabel" class="muted">{{ modelLabel }}</span>
      <span v-if="status.protocol" class="muted">{{ status.protocol }}</span>
      <span v-if="status.baseUrl" class="muted mono">{{ status.baseUrl }}</span>
      <span v-if="status.usingPlatformDefault" class="muted">{{ platformDefaultText }}</span>
    </div>
  </el-alert>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AiRuntimeStatusVO } from '@/api/tool/ai/catalog'
import { useToolAiI18n } from '@/composables/useToolAiI18n'

const props = defineProps<{
  status: AiRuntimeStatusVO | null
  aiReady: boolean
}>()

const { gp } = useToolAiI18n()

const titleText = computed(() => gp('runtimeActive'))
const platformDefaultText = computed(() => gp('runtimePlatformDefault'))

const modelLabel = computed(() => {
  const s = props.status
  if (!s) return ''
  if (s.modelName && s.modelCode) return `${s.modelName} (${s.modelCode})`
  return s.modelName || s.modelCode || ''
})
</script>

<style scoped lang="scss">
.ai-runtime-banner {
  margin-bottom: 16px;
}

.ai-runtime-banner__title {
  font-weight: 600;
}

.ai-runtime-banner__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 14px;
  margin-top: 4px;
  font-size: 13px;
}

.muted {
  color: var(--el-text-color-secondary);
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  word-break: break-all;
}
</style>
