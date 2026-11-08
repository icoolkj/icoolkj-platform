<template>
  <div class="gen-page-shell">
    <GenListLayout
      :title="gp('menuTable')"
      :description="gp('tableDescription')"
      :show-search="false"
      :show-right-toolbar="false"
    >
      <el-alert v-if="!aiReady" type="warning" :closable="false" show-icon class="form-block-tip">
        {{ gp('notEnabled') }}
      </el-alert>
      <AiRuntimeBanner :status="status" :ai-ready="aiReady" />
      <TableWizard :ai-ready="aiReady" />
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { useToolAiI18n } from '@/composables/useToolAiI18n'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import TableWizard from '../TableWizard.vue'
import AiRuntimeBanner from '../components/AiRuntimeBanner.vue'
import { useAiReady } from '@/composables/useAiReady'

const { gp } = useToolAiI18n()
const { aiReady, status } = useAiReady('chat')
</script>

<script lang="ts">
export default { name: 'ToolAiTable' }
</script>

<style scoped lang="scss">
@use '@/styles/tool-ai.scss';
</style>
