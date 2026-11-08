<template>
  <el-form
    ref="formRef"
    :model="modelValue"
    :rules="rules"
    :label-width="labelWidth"
    @submit.prevent
  >
    <template v-for="(group, groupIndex) in fieldGroups" :key="`${group.name}-${groupIndex}`">
      <el-divider v-if="group.name" content-position="left">{{ group.name }}</el-divider>
      <el-row :gutter="16">
        <el-col
          v-for="field in visibleGroupFields(group.fields)"
          :key="field.fieldCode"
          :span="oaColSpan(field)"
        >
          <el-form-item :label="labelOf(field)" :prop="field.fieldCode">
            <FormFieldControl
              :field="toFormField(field)"
              :model-value="modelValue[field.fieldCode!]"
              :placeholder="placeholderOf(field)"
              :disabled="field.fieldPerm === 'R'"
              :host="host"
              :get-dict="getDict"
              @update:model-value="updateField(field.fieldCode!, $event)"
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
        @update:model-value="emit('update:remark', String($event ?? ''))"
      />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from 'element-plus'
import FormFieldControl from '@/components/form-designer/runtime/FormFieldControl.vue'
import {
  buildRulesForOaFields,
  isSelectLikeComponent,
  mapFormComponent,
} from '@/components/form-designer/runtime/formFieldCore'
import { oaFieldToFormField } from '@/components/form-designer/oaBridge'
import { useDict } from '@/composables/useDict'
import type { FormFieldDef, FormRuntimeHostContext } from '@/types/form-designer/schema'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'

const props = withDefaults(
  defineProps<{
    fields: OaFieldDefVO[]
    modelValue: Record<string, unknown>
    /** OA 宿主上下文（fileBizType / downloadScope） */
    host?: FormRuntimeHostContext
    remark?: string
    appendRemark?: boolean
    remarkLabel?: string
    remarkPlaceholder?: string
    labelWidth?: string
    resolveLabel?: (field: OaFieldDefVO) => string
    resolvePlaceholder?: (field: OaFieldDefVO) => string
    resolveRequiredMessage?: (field: OaFieldDefVO) => string
    resolveMaxLengthMessage?: (field: OaFieldDefVO, max: number) => string
  }>(),
  {
    appendRemark: false,
    labelWidth: '108px',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, unknown>]
  'update:remark': [value: string]
}>()

const { t } = useI18n()
const remarkLabelText = computed(() => props.remarkLabel || t('common.remark'))
const remarkPlaceholderText = computed(
  () => props.remarkPlaceholder || t('common.optionalRemark'),
)
const formRef = ref<FormInstance>()
const fieldGroups = computed(() => {
  const groups: Array<{ name: string; fields: OaFieldDefVO[] }> = []
  for (const field of props.fields) {
    const name = field.fieldGroup?.trim() ?? ''
    const last = groups.at(-1)
    if (!last || last.name !== name) {
      groups.push({ name, fields: [field] })
    } else {
      last.fields.push(field)
    }
  }
  return groups
})

function visibleGroupFields(list: OaFieldDefVO[]) {
  return list.filter((f) => f.fieldPerm !== 'H')
}

function toFormField(field: OaFieldDefVO): FormFieldDef {
  return oaFieldToFormField(field)
}

/** No-layout fallback: implicit full-width stack (L0 colSpan is not a grid SoT). */
function oaColSpan(_field: OaFieldDefVO): number {
  return 24
}

function updateField(code: string, value: unknown) {
  const field = props.fields.find((f) => f.fieldCode === code)
  if (field?.fieldPerm === 'R') return
  emit('update:modelValue', { ...props.modelValue, [code]: value })
}

const dictTypes = computed(() =>
  props.fields.map((f) => f.dictType).filter((d): d is string => !!d && d.length > 0),
)
const { getDict } = useDict(dictTypes)

function labelOf(field: OaFieldDefVO) {
  const fromResolver = props.resolveLabel?.(field)?.trim()
  if (fromResolver) return fromResolver
  return field.fieldName || field.fieldCode || ''
}

const rules = computed<FormRules>(() =>
  buildRulesForOaFields(props.fields, {
    resolveLabel: props.resolveLabel,
    resolveRequiredMessage: props.resolveRequiredMessage,
    resolveMaxLengthMessage: props.resolveMaxLengthMessage,
    pleaseInput: (label) => t('common.pleaseInput', { field: label }),
    pleaseSelect: (label) => t('common.pleaseSelect', { field: label }),
  }),
)

watch(
  () => props.fields,
  (list) => {
    const next = { ...props.modelValue }
    for (const field of list) {
      if (!field.fieldCode) continue
      if (
        next[field.fieldCode] === undefined &&
        field.defaultValue != null &&
        field.defaultValue !== ''
      ) {
        next[field.fieldCode] = coerceDefault(field)
      }
    }
    emit('update:modelValue', next)
  },
  { immediate: true, deep: true },
)

function coerceDefault(field: OaFieldDefVO) {
  const raw = field.defaultValue
  if (raw == null) return undefined
  const component = mapFormComponent(field.componentType)
  if (component === 'input-number') {
    const n = Number(raw)
    return Number.isFinite(n) ? n : raw
  }
  return raw
}

function placeholderOf(field: OaFieldDefVO) {
  const fromResolver = props.resolvePlaceholder?.(field)?.trim()
  if (fromResolver) return fromResolver
  const name = labelOf(field)
  const component = mapFormComponent(field.componentType)
  if (isSelectLikeComponent(component)) {
    return t('common.pleaseSelect', { field: name })
  }
  return t('common.pleaseInput', { field: name })
}

async function validate() {
  const ok = await formRef.value?.validate().catch(() => false)
  return !!ok
}

defineExpose({ validate })
</script>
