<template>
  <el-form
    ref="formRef"
    class="form-runtime"
    :class="[labelAlignClass, { 'is-design': design }]"
    :model="formModel"
    :rules="rules"
    :label-width="effectiveLabelWidth"
    :label-position="labelPosition"
    :size="formSize"
    :hide-required-asterisk="hideRequiredAsterisk"
    @submit.prevent
  >
    <template v-for="node in visibleNodes" :key="node.id">
      <el-divider
        v-if="node.type === 'divider'"
        content-position="left"
        class="form-runtime__divider"
        :class="{ 'is-design-active': design && selectedNodeId === node.id }"
        @click.stop="onSelectNode(node.id)"
      >
        {{ node.title || '' }}
      </el-divider>

      <el-card
        v-else-if="node.type === 'card'"
        class="form-runtime__card"
        :class="{ 'is-design-active': design && selectedNodeId === node.id && !selectedFieldId }"
        shadow="never"
        :header="node.title || undefined"
        @click.stop="onSelectNode(node.id)"
      >
        <el-row :gutter="16">
          <el-col
            v-for="field in visibleFields(node.children)"
            :key="field.id"
            :span="colSpanOf(field)"
            class="form-runtime__col"
            :class="{ 'is-design-active': design && selectedFieldId === field.id }"
            @click.stop="onSelectField(node.id, field.id)"
          >
            <el-form-item
              :label="labelOf(field)"
              :prop="field.fieldCode"
              :required="isRequiredVisual(field)"
            >
              <FormFieldControl
                :field="field"
                :model-value="modelValue[field.fieldCode]"
                :placeholder="placeholderOf(field)"
                :disabled="design || isReadonly(field)"
                :host="host"
                :get-dict="getDict"
                @update:model-value="updateField(field.fieldCode, $event)"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-row
        v-else-if="node.type === 'row'"
        :gutter="16"
        class="form-runtime__row"
        :class="{ 'is-design-active': design && selectedNodeId === node.id && !selectedFieldId }"
        @click.stop="onSelectNode(node.id)"
      >
        <el-col
          v-for="field in visibleFields(node.children)"
          :key="field.id"
          :span="colSpanOf(field)"
          class="form-runtime__col"
          :class="{ 'is-design-active': design && selectedFieldId === field.id }"
          @click.stop="onSelectField(node.id, field.id)"
        >
          <el-form-item
            :label="labelOf(field)"
            :prop="field.fieldCode"
            :required="isRequiredVisual(field)"
          >
            <FormFieldControl
              :field="field"
              :model-value="modelValue[field.fieldCode]"
              :placeholder="placeholderOf(field)"
              :disabled="design || isReadonly(field)"
              :host="host"
              :get-dict="getDict"
              @update:model-value="updateField(field.fieldCode, $event)"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row
        v-else-if="node.type === 'field'"
        :gutter="16"
        class="form-runtime__row"
        @click.stop="onSelectField(node.id, node.field.id)"
      >
        <el-col
          :span="colSpanOf(node.field)"
          class="form-runtime__col"
          :class="{ 'is-design-active': design && selectedFieldId === node.field.id }"
        >
          <el-form-item
            :label="labelOf(node.field)"
            :prop="node.field.fieldCode"
            :required="isRequiredVisual(node.field)"
          >
            <FormFieldControl
              :field="node.field"
              :model-value="modelValue[node.field.fieldCode]"
              :placeholder="placeholderOf(node.field)"
              :disabled="design || isReadonly(node.field)"
              :host="host"
              :get-dict="getDict"
              @update:model-value="updateField(node.field.fieldCode, $event)"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </template>

    <el-form-item v-if="appendRemark" :label="remarkLabelText" prop="__remark">
      <el-input
        :model-value="remark ?? ''"
        type="textarea"
        :rows="2"
        maxlength="500"
        show-word-limit
        :placeholder="remarkPlaceholderText"
        :disabled="readonly || design"
        @update:model-value="emit('update:remark', String($event ?? ''))"
      />
    </el-form-item>

    <el-empty v-if="!visibleNodes.length && !appendRemark" :description="emptyText" :image-size="72" />
  </el-form>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from 'element-plus'
import FormFieldControl from '@/components/form-designer/runtime/FormFieldControl.vue'
import {
  buildRulesForFields,
  elColSpan,
  labelOfFormField,
  placeholderOfFormField,
} from '@/components/form-designer/runtime/formFieldCore'
import { flattenFields } from '@/components/form-designer/schemaUtils'
import { useDict } from '@/composables/useDict'
import type {
  FormDocument,
  FormFieldDef,
  FormPreviewDevice,
  FormRuntimeHostContext,
} from '@/types/form-designer/schema'

const props = withDefaults(
  defineProps<{
    document: FormDocument
    modelValue: Record<string, unknown>
    device?: FormPreviewDevice
    emptyText?: string
    /** Global read-only (approve view). */
    readonly?: boolean
    appendRemark?: boolean
    remark?: string
    remarkLabel?: string
    remarkPlaceholder?: string
    host?: FormRuntimeHostContext
    /** Designer WYSIWYG: same layout as preview, selection chrome only. */
    design?: boolean
    selectedFieldId?: string
    selectedNodeId?: string
  }>(),
  {
    device: 'pc',
    emptyText: 'Empty form',
    readonly: false,
    appendRemark: false,
    design: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, unknown>]
  'update:remark': [value: string]
  selectField: [nodeId: string, fieldId: string]
  selectNode: [nodeId: string]
}>()

const { t } = useI18n()
const formRef = ref<FormInstance>()
const dictTypes = computed(() =>
  flattenFields(props.document)
    .map((f) => f.dictType)
    .filter((d): d is string => !!d && d.length > 0),
)
const { getDict } = useDict(dictTypes)

const formModel = computed(() => props.modelValue)
const remarkLabelText = computed(() => props.remarkLabel || t('common.remark'))
const remarkPlaceholderText = computed(
  () => props.remarkPlaceholder || t('common.optionalRemark'),
)

const visibleNodes = computed(() =>
  props.document.nodes.filter((node) => {
    if (node.type === 'divider') return true
    if (node.type === 'field') return node.field.fieldPerm !== 'H'
    if (node.type === 'row' || node.type === 'card') {
      if (!node.children.length) return node.type === 'card'
      return node.children.some((c) => c.fieldPerm !== 'H')
    }
    return false
  }),
)

const labelPosition = computed(() =>
  props.device === 'mobile' ? 'top' : props.document.form.labelPosition,
)

const formSize = computed(() => props.document.form.size || 'default')
const hideRequiredAsterisk = computed(() => Boolean(props.document.form.hideRequiredAsterisk))
const labelAlignClass = computed(() => {
  if (labelPosition.value !== 'left') return undefined
  return props.document.form.labelAlign === 'left'
    ? 'form-runtime--label-left'
    : 'form-runtime--label-right'
})

const effectiveLabelWidth = computed(() => {
  if (labelPosition.value === 'top') return 'auto'
  return `${props.document.form.labelWidth || 100}px`
})

const rules = computed<FormRules>(() =>
  buildRulesForFields(flattenFields(props.document), {
    host: props.host,
    readonly: props.readonly || props.design,
    pleaseInput: (label) => t('common.pleaseInput', { field: label }),
    pleaseSelect: (label) => t('common.pleaseSelect', { field: label }),
  }),
)

function visibleFields(fields: FormFieldDef[]) {
  return fields.filter((f) => f.fieldPerm !== 'H')
}

function isReadonly(field: FormFieldDef) {
  return props.readonly || field.fieldPerm === 'R'
}

function isRequiredVisual(field: FormFieldDef) {
  return Boolean(field.required && !isReadonly(field) && !props.design)
}

function colSpanOf(field: FormFieldDef): number {
  return elColSpan(field, props.device)
}

function labelOf(field: FormFieldDef) {
  return labelOfFormField(field, props.host)
}

function placeholderOf(field: FormFieldDef) {
  return placeholderOfFormField(
    field,
    props.host,
    (label) => t('common.pleaseSelect', { field: label }),
    (label) => t('common.pleaseInput', { field: label }),
  )
}

function updateField(code: string, value: unknown) {
  if (props.readonly || props.design) return
  emit('update:modelValue', { ...props.modelValue, [code]: value })
}

function onSelectField(nodeId: string, fieldId: string) {
  if (!props.design) return
  emit('selectField', nodeId, fieldId)
}

function onSelectNode(nodeId: string) {
  if (!props.design) return
  emit('selectNode', nodeId)
}

async function validate() {
  const ok = await formRef.value?.validate().catch(() => false)
  return !!ok
}

defineExpose({ validate })
</script>

<style scoped>
.form-runtime {
  width: 100%;
}
.form-runtime--label-left :deep(.el-form-item__label) {
  justify-content: flex-start;
}
.form-runtime--label-right :deep(.el-form-item__label) {
  justify-content: flex-end;
}
.form-runtime__row {
  width: 100%;
}
.form-runtime__card {
  margin-bottom: 12px;
  border: 1px solid var(--el-border-color-lighter);
}
.form-runtime__card :deep(.el-card__header) {
  padding: 10px 14px;
  font-weight: 600;
  font-size: 13px;
}
.form-runtime__card :deep(.el-card__body) {
  padding: 14px 14px 4px;
}
.form-runtime__divider {
  margin: 8px 0 16px;
}

/* Controls fill their grid column (number/date/select default to intrinsic width). */
.form-runtime :deep(.el-form-item__content) {
  min-width: 0;
}
.form-runtime :deep(.el-form-item__content > *) {
  width: 100%;
  max-width: 100%;
}
.form-runtime :deep(.el-input-number) {
  width: 100%;
}
.form-runtime :deep(.el-date-editor.el-input),
.form-runtime :deep(.el-date-editor.el-input__wrapper),
.form-runtime :deep(.el-select) {
  width: 100%;
}
.form-runtime :deep(.el-switch) {
  width: auto;
  max-width: none;
}

.form-runtime.is-design :deep(.el-form-item) {
  margin-bottom: 14px;
  pointer-events: none;
}
.form-runtime.is-design .form-runtime__col {
  cursor: pointer;
  border-radius: 4px;
  transition: box-shadow 0.12s ease, background 0.12s ease;
}
.form-runtime.is-design .form-runtime__col :deep(.el-form-item) {
  pointer-events: none;
}
.form-runtime.is-design .form-runtime__col.is-design-active {
  box-shadow: 0 0 0 1px var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.form-runtime.is-design .form-runtime__divider.is-design-active,
.form-runtime.is-design .form-runtime__card.is-design-active,
.form-runtime.is-design .form-runtime__row.is-design-active {
  box-shadow: 0 0 0 1px var(--el-color-primary-light-5);
}
</style>
