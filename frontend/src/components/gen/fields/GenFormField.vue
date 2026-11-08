<template>
  <component
    :is="widgetComponent"
    :ctx="fieldCtx"
    @update:model-value="emit('update:modelValue', $event)"
    @enter="emit('enter')"
    @avatar-uploaded="emit('avatar-uploaded', $event)"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getFieldWidget } from '@/components/gen/registry/field-widgets/registry'
import type { GenFieldWidgetContext } from '@/components/gen/registry/field-widgets/types'
import { NO, YES } from '@/constants/flags'
import type { UserProfile } from '@/types/profile'
import type { DictOption } from '@/types/system/dict'
import type { GenViewFormField, GenViewQueryField } from '@/types/tool/genView'

const props = defineProps<{
  config: GenViewQueryField | GenViewFormField
  modelValue?: unknown
  placeholder?: string
  mode?: 'query' | 'form'
  disabled?: boolean
  getDict?: (dictType: string) => DictOption[]
  getFieldOptions?: (field: string) => DictOption[] | undefined
  treeData?: Record<string, unknown>[]
  treeProps?: { label: string; value: string; children: string }
  formModel?: Record<string, unknown>
  gp?: (key: string, params?: Record<string, string | number>) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
  enter: []
  'avatar-uploaded': [payload: UserProfile]
}>()

const { t } = useI18n()
const formConfig = computed(() => props.config as GenViewFormField)
const gpFn = computed(() => props.gp ?? ((key: string) => key))

const dictOptions = computed(() => {
  const field = props.config.field
  if (props.getFieldOptions) {
    const custom = props.getFieldOptions(field)
    if (custom?.length) return custom
  }
  const dictType = props.config.dictType
  if (!dictType || !props.getDict) return []
  return props.getDict(dictType)
})

const resolvedComponent = computed(() => {
  const component = props.config.component
  const treeSource = 'treeSource' in props.config ? props.config.treeSource : undefined
  if (treeSource && (!component || component === 'input')) {
    return 'tree-select'
  }
  if (props.config.dictType && !component) {
    return props.mode === 'form' ? 'radio' : 'select'
  }
  if (props.config.dictType && component === 'input') {
    return props.mode === 'form' ? 'radio' : 'select'
  }
  return component ?? 'input'
})

function resolveDictModelValue(raw: unknown, options: DictOption[]): unknown {
  if (raw == null || raw === '') return raw
  const str = String(raw)
  if (options.some((opt) => String(opt.value) === str)) return str
  if (str === YES && options.some((opt) => opt.value === 'Y')) return 'Y'
  if (str === NO && options.some((opt) => opt.value === 'N')) return 'N'
  return str
}

const dictModelValue = computed(() => resolveDictModelValue(props.modelValue, dictOptions.value))

function emitDictValue(value: unknown) {
  emit('update:modelValue', value)
}

function splitCsv(value: unknown): string[] {
  if (value == null || value === '') return []
  return String(value)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

const checkboxValues = computed(() => splitCsv(props.modelValue))

function emitCheckboxValues(values: string[]) {
  emit('update:modelValue', values.length ? values.join(',') : '')
}

const dateRangeModel = computed<[string, string] | undefined>(() => {
  const v = props.modelValue
  if (!Array.isArray(v) || v.length < 2) return undefined
  const a = v[0] == null || v[0] === '' ? '' : String(v[0])
  const b = v[1] == null || v[1] === '' ? '' : String(v[1])
  if (!a && !b) return undefined
  return [a, b]
})

function emitDateRange(value: unknown) {
  if (value == null || value === '') {
    emit('update:modelValue', undefined)
    return
  }
  if (Array.isArray(value) && value.length >= 2) {
    emit('update:modelValue', [String(value[0] ?? ''), String(value[1] ?? '')])
    return
  }
  emit('update:modelValue', undefined)
}

const fieldStyle = computed(() => {
  if (props.mode === 'form') return undefined
  const width = 'width' in props.config ? props.config.width : undefined
  return width ? { width: `${width}px` } : { width: '200px' }
})

const treePropsResolved = computed(
  () => props.treeProps ?? { label: 'label', value: 'id', children: 'children' },
)

const fieldCtx = computed(
  (): GenFieldWidgetContext => ({
    config: props.config,
    formConfig: formConfig.value,
    modelValue: props.modelValue,
    placeholder: props.placeholder,
    mode: props.mode ?? 'form',
    disabled: props.disabled,
    dictOptions: dictOptions.value,
    dictModelValue: dictModelValue.value,
    getDict: props.getDict,
    getFieldOptions: props.getFieldOptions,
    fieldStyle: fieldStyle.value,
    treeData: props.treeData,
    treeProps: treePropsResolved.value,
    formModel: props.formModel,
    gp: gpFn.value,
    dateRangeModel: dateRangeModel.value,
    dateRangeStartPlaceholder: t('common.dateRangeStart'),
    dateRangeEndPlaceholder: t('common.dateRangeEnd'),
    checkboxValues: checkboxValues.value,
    emitDictValue,
    emitDateRange,
    emitCheckboxValues,
  }),
)

const widgetComponent = computed(() => {
  const id = resolvedComponent.value
  const registered = getFieldWidget(id)
  if (!registered && import.meta.env.DEV) {
    console.warn(`[GenFormField] unregistered field widget "${id}", fallback to input`)
  }
  return registered ?? getFieldWidget('input')
})
</script>
