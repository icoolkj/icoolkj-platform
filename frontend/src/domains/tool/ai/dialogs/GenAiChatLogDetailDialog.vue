<!-- AI 对话记录详情积木：由 view.extensions.dialogs 挂载 -->
<template>
  <el-dialog
    :model-value="modelValue"
    :title="title || gp('logDetail')"
    width="72%"
    top="4vh"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading">
      <el-descriptions v-if="detail" :column="2" border class="detail-meta">
        <el-descriptions-item :label="gf('title', 'label')">
          {{ detail.title || gp('newChat') }}
        </el-descriptions-item>
        <el-descriptions-item :label="gf('operatorName', 'label')">
          {{ detail.operatorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="gf('createdAt', 'label')">
          {{ detail.createdAt || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="gf('updatedAt', 'label')">
          {{ detail.updatedAt || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="detail?.messages?.length" class="detail-messages">
        <div
          v-for="msg in detail.messages"
          :key="msg.id"
          class="detail-message"
          :class="msg.role"
        >
          <div class="detail-message-head">
            {{ msg.role === 'user' ? gp('roleUser') : gp('roleAssistant') }}
            <span>{{ msg.createdAt }}</span>
          </div>
          <AiLogTextBlock :text="msg.content" :rows="8" />
        </div>
      </div>
      <el-empty v-else :description="gp('chatLogEmptyHint')" />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import AiLogTextBlock from '@/domains/tool/ai/components/AiLogTextBlock.vue'
import { getAiChatSession, type ToolAiChatSession } from '@/api/tool/aiChat'
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue: boolean
  id?: string | null
  title?: string
}>()

const emit = defineEmits<{ 'update:modelValue': [boolean] }>()

const { gp, gf } = useGenPageShellI18n('aiChatLog')

const loading = ref(false)
const detail = ref<ToolAiChatSession | null>(null)

watch(
  () => [props.modelValue, props.id] as const,
  async ([open, id]) => {
    if (!open || !id) {
      detail.value = null
      return
    }
    loading.value = true
    try {
      const res = await getAiChatSession(id, false)
      detail.value = res.data.data
    } finally {
      loading.value = false
    }
  },
)
</script>

<style scoped>
.detail-meta {
  margin-bottom: 16px;
}

.detail-messages {
  max-height: 60vh;
  overflow: auto;
}

.detail-message {
  margin-bottom: 16px;
}

.detail-message.user :deep(.ai-log-text-block) {
  border-left: 3px solid var(--el-color-primary);
  padding-left: 12px;
}

.detail-message-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-regular);
}

.detail-message-head span {
  font-weight: 400;
  color: var(--el-text-color-secondary);
}
</style>
