<template>
  <div class="gen-page-shell chat-page">
    <GenListLayout
      plain
      :title="gp('menuChat')"
      :description="gp('chatDescription')"
      class="chat-page-inner"
    >
      <el-alert v-if="!aiReady" type="warning" :closable="false" show-icon class="form-block-tip">
        {{ gp('notEnabled') }}
      </el-alert>
      <AiRuntimeBanner :status="status" :ai-ready="aiReady" />
      <el-card shadow="never" class="table-card chat-card">
        <ChatPanel :ai-ready="aiReady" />
      </el-card>
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { useToolAiI18n } from '@/composables/useToolAiI18n'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import ChatPanel from '../components/ChatPanel.vue'
import AiRuntimeBanner from '../components/AiRuntimeBanner.vue'
import { useAiReady } from '@/composables/useAiReady'

const { gp } = useToolAiI18n()
const { aiReady, status } = useAiReady('chat')
</script>

<script lang="ts">
export default { name: 'ToolAiChat' }
</script>

<style scoped lang="scss">
@use '@/styles/tool-ai.scss';

.chat-page {
  height: calc(100vh - 56px);
  overflow: hidden;
  box-sizing: border-box;
}

.chat-page-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  box-sizing: border-box;
}

.chat-page-inner :deep(.page-header) {
  flex-shrink: 0;
  margin-bottom: 12px;
}

.chat-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.chat-card :deep(.el-card__body) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 0;
}
</style>
