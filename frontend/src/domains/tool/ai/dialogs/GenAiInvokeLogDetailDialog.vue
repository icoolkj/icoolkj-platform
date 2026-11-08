<!-- AI 调用记录详情积木：由 view.extensions.dialogs 挂载 -->
<template>
  <el-dialog
    :model-value="modelValue"
    :title="title || gp('logDetail')"
    width="78%"
    top="4vh"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading">
      <el-descriptions v-if="detail" :column="2" border class="detail-meta">
        <el-descriptions-item :label="gf('bizType', 'label')">
          <DictTag :options="getDict('tool_ai_invoke_biz_type')" :value="detail.bizType" />
        </el-descriptions-item>
        <el-descriptions-item :label="gf('model', 'label')">{{ detail.model }}</el-descriptions-item>
        <el-descriptions-item :label="gf('responseStatus', 'label')">
          <DictTag :options="getDict('tool_ai_invoke_status')" :value="detail.responseStatus" />
        </el-descriptions-item>
        <el-descriptions-item :label="gf('costTime', 'label')">
          {{ detail.costTime != null ? `${detail.costTime} ms` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="gf('operatorName', 'label')">
          {{ detail.operatorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="gf('createdAt', 'label')">
          {{ detail.createdAt || '-' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMessage" :label="gp('errorMessage')" :span="2">
          {{ detail.errorMessage }}
        </el-descriptions-item>
      </el-descriptions>

      <el-tabs v-if="detail" v-model="tab" type="border-card" class="detail-tabs">
        <el-tab-pane :label="gp('systemPrompt')" name="system">
          <AiLogTextBlock :text="detail.systemPrompt" :rows="16" />
        </el-tab-pane>
        <el-tab-pane :label="gp('userMessage')" name="user">
          <AiLogTextBlock :text="detail.userMessage" :rows="16" />
        </el-tab-pane>
        <el-tab-pane :label="gp('aiResponse')" name="response">
          <AiLogTextBlock :text="detail.responseContent" :rows="20" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import DictTag from '@/components/DictTag.vue'
import AiLogTextBlock from '@/domains/tool/ai/components/AiLogTextBlock.vue'
import { useDict } from '@/composables/useDict'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { getAiInvokeLog, type ToolAiInvokeLogVO } from '@/api/tool/aiLog'
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue: boolean
  id?: string | null
  title?: string
}>()

const emit = defineEmits<{ 'update:modelValue': [boolean] }>()

const { gp, gf } = useGenPageShellI18n('aiInvokeLog')
const { getDict } = useDict(['tool_ai_invoke_biz_type', 'tool_ai_invoke_status'])

const loading = ref(false)
const detail = ref<ToolAiInvokeLogVO | null>(null)
const tab = ref('user')

watch(
  () => [props.modelValue, props.id] as const,
  async ([open, id]) => {
    if (!open || !id) {
      detail.value = null
      return
    }
    loading.value = true
    tab.value = 'user'
    try {
      const res = await getAiInvokeLog(id)
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

.detail-tabs {
  margin-top: 8px;
}
</style>
