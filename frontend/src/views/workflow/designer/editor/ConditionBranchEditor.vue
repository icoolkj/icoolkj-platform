<template>
  <el-tabs v-model="activeTab" class="vfd-editor-tabs">
    <el-tab-pane :label="gp('designerTabBasic')" name="basic">
      <el-form-item :label="gp('designerNodeName')" prop="name">
        <el-input v-model="node.name" maxlength="100" />
      </el-form-item>
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        :title="gp('designerConditionPriorityTip')"
        style="margin-bottom: 12px"
      />
      <el-alert
        v-if="!conditionFields.length"
        type="info"
        :closable="false"
        show-icon
        :title="gp('designerConditionFieldsEmpty')"
        style="margin-bottom: 12px"
      />
      <el-form-item :label="gp('designerConditionMode')">
        <el-radio-group v-model="conditionMode" @change="emit('rebuild')">
          <el-radio value="compare">{{ gp('designerConditionModeCompare') }}</el-radio>
          <el-radio value="range">{{ gp('designerConditionModeRange') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="gp('designerConditionField')">
        <el-select
          v-model="conditionField"
          clearable
          filterable
          style="width: 100%"
          :disabled="!conditionFields.length"
          @change="emit('rebuild')"
        >
          <el-option
            v-for="f in conditionFields"
            :key="f.fieldCode"
            :label="`${f.fieldName} (${f.fieldCode})`"
            :value="f.fieldCode!"
          />
        </el-select>
      </el-form-item>
      <template v-if="conditionMode === 'compare'">
        <div class="cond-row">
          <el-form-item :label="gp('designerConditionOp')" class="cond-op">
            <el-select v-model="conditionOp" style="width: 100%" @change="emit('rebuild')">
              <el-option label=">" value=">" />
              <el-option label=">=" value=">=" />
              <el-option label="<" value="<" />
              <el-option label="<=" value="<=" />
              <el-option label="==" value="==" />
              <el-option label="!=" value="!=" />
            </el-select>
          </el-form-item>
          <el-form-item :label="gp('designerConditionValue')" class="cond-val">
            <el-input v-model="conditionValue" @change="emit('rebuild')" />
          </el-form-item>
        </div>
      </template>
      <template v-else>
        <div class="cond-row">
          <el-form-item :label="gp('designerConditionRangeMin')" class="cond-val">
            <el-input
              v-model="conditionValue"
              :placeholder="gp('designerConditionRangeMinPh')"
              @change="emit('rebuild')"
            />
          </el-form-item>
          <el-form-item :label="gp('designerConditionRangeMax')" class="cond-val">
            <el-input
              v-model="conditionValue2"
              :placeholder="gp('designerConditionRangeMaxPh')"
              @change="emit('rebuild')"
            />
          </el-form-item>
        </div>
        <p class="pass-mode-hint">{{ gp('designerConditionRangeHint') }}</p>
      </template>
    </el-tab-pane>

    <el-tab-pane :label="gp('designerTabAdvanced')" name="advanced">
      <el-form-item :label="gp('designerConditionExpr')" prop="conditionExpression">
        <el-input v-model="node.conditionExpression" type="textarea" :rows="4" />
        <p class="pass-mode-hint">{{ gp('designerConditionExprAdvancedHint') }}</p>
      </el-form-item>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import type { WorkflowFormFieldDef } from '@/types/workflow/formField'
import type { WorkflowDesignNode } from '@/types/workflow/design'
import { useDesignerGenI18n } from '@/composables/useDesignerGenI18n'

const node = defineModel<WorkflowDesignNode>({ required: true })
const activeTab = defineModel<string>('activeTab', { default: 'basic' })
const conditionMode = defineModel<'compare' | 'range'>('conditionMode', { default: 'compare' })
const conditionField = defineModel<string>('conditionField', { default: '' })
const conditionOp = defineModel<string>('conditionOp', { default: '>' })
const conditionValue = defineModel<string>('conditionValue', { default: '' })
const conditionValue2 = defineModel<string>('conditionValue2', { default: '' })

defineProps<{
  conditionFields: WorkflowFormFieldDef[]
}>()

const emit = defineEmits<{
  rebuild: []
}>()

const { gp } = useDesignerGenI18n()
</script>

<style scoped lang="scss">
@use '@/styles/vertical-flow-editor.scss';
</style>
