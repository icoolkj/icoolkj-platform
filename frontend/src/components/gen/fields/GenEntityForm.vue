<!--
  Gen 实体表单积木：按 view.form.fields 渲染字段（insert/edit 过滤由父组件传入 fields）。
  不负责弹窗壳与提交 API —— 由 GenFormShell + GenCrudPage / GenTreeCrudPage 编排。
-->
<template>
  <el-form
    ref="formRef"
    :model="model"
    :rules="rules"
    :label-width="labelWidth"
  >
    <el-form-item
      v-for="field in fields"
      :key="field.field"
      :label="gf(field.field, 'label')"
      :prop="field.field"
    >
      <GenFormField
        :model-value="model[field.field]"
        :config="field"
        mode="form"
        :placeholder="gf(field.field, 'placeholder')"
        :disabled="isDisabled(field)"
        :get-dict="getDict"
        :get-field-options="getFieldOptions"
        :tree-data="resolveTreeData(field)"
        :tree-props="resolveTreeProps(field)"
        :form-model="model"
        :gp="gp"
        @update:model-value="(v) => onFieldUpdate(field.field, v)"
      />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import GenFormField from '@/components/gen/fields/GenFormField.vue'
import type { DictOption } from '@/types/system/dict'
import type { GenViewFormField } from '@/types/tool/genView'

const props = defineProps<{
  model: Record<string, unknown>
  fields: GenViewFormField[]
  rules?: FormRules
  labelWidth?: string
  gf: (field: string, key?: 'label' | 'placeholder' | 'placeholderReject') => string
  gp?: (key: string, params?: Record<string, string | number>) => string
  getDict: (dictType: string) => DictOption[]
  getFieldOptions?: (field: string) => DictOption[] | undefined
  treeSources?: Record<string, Record<string, unknown>[]>
  isFieldDisabled?: (field: GenViewFormField) => boolean
}>()

const formRef = ref<FormInstance>()

function isDisabled(field: GenViewFormField) {
  return props.isFieldDisabled?.(field) ?? false
}

function onFieldUpdate(field: string, value: unknown) {
  props.model[field] = value
}

function resolveTreeData(field: GenViewFormField) {
  const treeSource = field.treeSource
  if (field.component !== 'tree-select' && !treeSource) return []
  const key = treeSource ?? 'self'
  return props.treeSources?.[key] ?? []
}

function resolveTreeProps(field: GenViewFormField) {
  return {
    label: field.treeLabelField || 'label',
    value: field.treeValueField || 'id',
    children: 'children',
  }
}

async function validate() {
  if (!formRef.value) return false
  return formRef.value.validate().catch(() => false)
}

function resetFields() {
  formRef.value?.resetFields()
}

defineExpose({ formRef, validate, resetFields })
</script>
