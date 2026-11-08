<template>
  <el-tabs v-model="activeTab" class="vfd-editor-tabs">
    <el-tab-pane :label="gp('designerTabBasic')" name="basic">
      <el-form-item :label="gp('designerNodeName')" prop="name">
        <el-input v-model="node.name" maxlength="100" />
      </el-form-item>
      <el-alert
        v-if="hintTitle"
        type="info"
        :closable="false"
        show-icon
        :title="hintTitle"
        style="margin-bottom: 12px"
      />
    </el-tab-pane>
    <el-tab-pane :label="gp('designerTabAdvanced')" name="advanced">
      <el-form-item :label="gp('wizardTaskKey')" prop="taskDefinitionKey">
        <el-input
          v-model="node.taskDefinitionKey"
          maxlength="64"
          @blur="emit('sync-variable-from-key')"
        />
      </el-form-item>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { WorkflowDesignNode } from '@/types/workflow/design'
import { useDesignerGenI18n } from '@/composables/useDesignerGenI18n'

const node = defineModel<WorkflowDesignNode>({ required: true })
const activeTab = defineModel<string>('activeTab', { default: 'basic' })

const props = defineProps<{
  /** condition | inclusive | parallel */
  variant: 'condition' | 'inclusive' | 'parallel'
}>()

const emit = defineEmits<{
  'sync-variable-from-key': []
}>()

const { gp } = useDesignerGenI18n()

const hintTitle = computed(() => {
  if (props.variant === 'parallel') return gp('designerParallelHint')
  if (props.variant === 'inclusive') return gp('designerInclusiveHint')
  return gp('designerConditionBranchesHint')
})
</script>

<style scoped lang="scss">
@use '@/styles/vertical-flow-editor.scss';
</style>
