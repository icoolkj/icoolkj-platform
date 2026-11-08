<template>
  <div>
    <el-form-item :label="gp('designerNodeName')" prop="name">
      <el-input v-model="node.name" maxlength="100" />
    </el-form-item>
    <el-alert
      type="info"
      :closable="false"
      show-icon
      :title="gp('designerBranchDefaultEditorHint')"
      style="margin-bottom: 12px"
    />
  </div>
</template>

<script setup lang="ts">
import type { WorkflowDesignNode } from '@/types/workflow/design'
import { useDesignerGenI18n } from '@/composables/useDesignerGenI18n'

const node = defineModel<WorkflowDesignNode>({ required: true })
const { gp } = useDesignerGenI18n()
</script>
