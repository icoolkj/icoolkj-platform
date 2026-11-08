<!-- AI 建表记录详情积木：由 view.extensions.dialogs 挂载 -->
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
        <el-descriptions-item :label="gf('recordType', 'label')">
          <DictTag :options="getDict('tool_ai_ddl_record_type')" :value="detail.recordType" />
        </el-descriptions-item>
        <el-descriptions-item :label="gf('status', 'label')">
          <DictTag :options="getDict('tool_ai_ddl_status')" :value="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item :label="gf('apiModule', 'label')">
          {{ detail.apiModule || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="gf('targetTableName', 'label')">
          {{ detail.targetTableName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="gf('operatorName', 'label')">
          {{ detail.operatorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="gf('createdAt', 'label')">
          {{ detail.createdAt || '-' }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="detail.generatedSummary"
          :label="gf('generatedSummary', 'label')"
          :span="2"
        >
          {{ detail.generatedSummary }}
        </el-descriptions-item>
      </el-descriptions>

      <el-tabs v-if="detail" v-model="tab" type="border-card" class="detail-tabs">
        <el-tab-pane :label="gp('userMessage')" name="prompt">
          <AiLogTextBlock :text="detail.userPrompt" :rows="10" />
        </el-tab-pane>
        <el-tab-pane :label="gp('generatedDdl')" name="generated">
          <AiLogTextBlock :text="detail.generatedDdl" :rows="18" />
        </el-tab-pane>
        <el-tab-pane v-if="detail.finalDdl" :label="gp('finalDdl')" name="final">
          <AiLogTextBlock :text="detail.finalDdl" :rows="18" />
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
import { getAiDdlLog, type ToolAiDdlRecordVO } from '@/api/tool/aiLog'
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue: boolean
  id?: string | null
  title?: string
}>()

const emit = defineEmits<{ 'update:modelValue': [boolean] }>()

const { gp, gf } = useGenPageShellI18n('aiDdlLog')
const { getDict } = useDict(['tool_ai_ddl_record_type', 'tool_ai_ddl_status'])

const loading = ref(false)
const detail = ref<ToolAiDdlRecordVO | null>(null)
const tab = ref('generated')

watch(
  () => [props.modelValue, props.id] as const,
  async ([open, id]) => {
    if (!open || !id) {
      detail.value = null
      return
    }
    loading.value = true
    tab.value = 'generated'
    try {
      const res = await getAiDdlLog(id)
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
