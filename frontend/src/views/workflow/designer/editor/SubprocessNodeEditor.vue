<template>
  <el-tabs v-model="activeTab" class="vfd-editor-tabs">
    <el-tab-pane :label="gp('designerTabBasic')" name="basic">
      <el-form-item :label="gp('designerNodeName')" prop="name">
        <el-input v-model="node.name" maxlength="100" />
      </el-form-item>
      <el-form-item :label="gp('designerStrategyConfig')" prop="strategyConfig">
        <el-input v-model="node.strategyConfig" type="textarea" :rows="4" />
        <p class="pass-mode-hint">{{ gp('designerCalledElementHint') }}</p>
      </el-form-item>
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
import type { WorkflowDesignNode } from '@/types/workflow/design'
import { useDesignerGenI18n } from '@/composables/useDesignerGenI18n'

const node = defineModel<WorkflowDesignNode>({ required: true })
const activeTab = defineModel<string>('activeTab', { default: 'basic' })

const emit = defineEmits<{
  'sync-variable-from-key': []
}>()

const { gp } = useDesignerGenI18n()
</script>

<style scoped lang="scss">
@use '@/styles/vertical-flow-editor.scss';
</style>
