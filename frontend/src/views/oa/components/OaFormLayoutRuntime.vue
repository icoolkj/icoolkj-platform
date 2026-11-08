<!-- Bound Runtime Host: buildBoundRuntimeDocument → FormRuntime; else OaDynamicForm.
     有 richText 时拆 Tab：基本信息 | 正文（固定高度，避免弹窗内 wangEditor 被挤扁） -->
<template>
  <div class="oa-form-layout-runtime">
    <el-tabs
      v-if="richTextFields.length"
      v-model="activeTab"
      class="oa-form-layout-runtime__tabs"
    >
      <el-tab-pane :label="basicTabLabel" name="basic">
        <FormRuntime
          v-if="runtimeDocument"
          ref="runtimeRef"
          v-model="modelProxy"
          v-model:remark="remarkProxy"
          :document="runtimeDocument"
          device="pc"
          :readonly="readonly"
          :append-remark="appendRemark"
          :remark-label="remarkLabel"
          :remark-placeholder="remarkPlaceholder"
          :host="hostContext"
        />
        <OaDynamicForm
          v-else
          ref="dynamicRef"
          v-model="modelProxy"
          v-model:remark="remarkProxy"
          :fields="basicFields"
          :host="hostContext"
          :append-remark="appendRemark"
          :remark-label="remarkLabel"
          :remark-placeholder="remarkPlaceholder"
          :resolve-label="resolveLabel"
          :resolve-placeholder="resolvePlaceholder"
          :resolve-required-message="resolveRequiredMessage"
          :resolve-max-length-message="resolveMaxLengthMessage"
        />
      </el-tab-pane>
      <el-tab-pane
        v-for="field in richTextFields"
        :key="field.fieldCode"
        :label="labelOf(field)"
        :name="richTabName(field)"
        lazy
      >
        <el-form
          :ref="(el: FormInstance | null) => setRichFormRef(field.fieldCode!, el)"
          class="oa-form-layout-runtime__rich-form"
          :model="modelProxy"
          :rules="richRulesOf(field)"
          label-position="top"
          @submit.prevent
        >
          <el-form-item :prop="field.fieldCode" :label="labelOf(field)">
            <FormFieldControl
              :field="toFormField(field)"
              :model-value="modelProxy[field.fieldCode!]"
              :placeholder="placeholderOf(field)"
              :disabled="readonly || field.fieldPerm === 'R'"
              :host="hostContext"
              :get-dict="getDict"
              @update:model-value="updateRichField(field.fieldCode!, $event)"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <template v-else>
      <FormRuntime
        v-if="runtimeDocument"
        ref="runtimeRef"
        v-model="modelProxy"
        v-model:remark="remarkProxy"
        :document="runtimeDocument"
        device="pc"
        :readonly="readonly"
        :append-remark="appendRemark"
        :remark-label="remarkLabel"
        :remark-placeholder="remarkPlaceholder"
        :host="hostContext"
      />
      <OaDynamicForm
        v-else
        ref="dynamicRef"
        v-model="modelProxy"
        v-model:remark="remarkProxy"
        :fields="fields"
        :host="hostContext"
        :append-remark="appendRemark"
        :remark-label="remarkLabel"
        :remark-placeholder="remarkPlaceholder"
        :resolve-label="resolveLabel"
        :resolve-placeholder="resolvePlaceholder"
        :resolve-required-message="resolveRequiredMessage"
        :resolve-max-length-message="resolveMaxLengthMessage"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from 'element-plus'
import FormRuntime from '@/components/form-designer/FormRuntime.vue'
import { buildBoundRuntimeDocument } from '@/components/form-designer/boundHost'
import { oaFieldToFormField } from '@/components/form-designer/oaBridge'
import FormFieldControl from '@/components/form-designer/runtime/FormFieldControl.vue'
import { isBlankRichText } from '@/components/form-designer/runtime/formFieldCore'
import { FILE_BIZ_OA_INSTANCE, FILE_DOWNLOAD_SCOPE_INFRA } from '@/constants/file'
import { isYes } from '@/constants/flags'
import { useDict } from '@/composables/useDict'
import OaDynamicForm from '@/views/oa/components/OaDynamicForm.vue'
import type { FormFieldDef, FormRuntimeHostContext } from '@/types/form-designer/schema'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'

const props = withDefaults(
  defineProps<{
    modelValue: Record<string, unknown>
    /** Full contract field pool (never writable-only / never strip file). */
    fields: OaFieldDefVO[]
    layoutSnapshotJson?: string | null
    readonly?: boolean
    remark?: string
    appendRemark?: boolean
    remarkLabel?: string
    remarkPlaceholder?: string
    resolveLabel?: (field: OaFieldDefVO) => string
    resolvePlaceholder?: (field: OaFieldDefVO) => string
    resolveRequiredMessage?: (field: OaFieldDefVO) => string
    resolveMaxLengthMessage?: (field: OaFieldDefVO, max: number) => string
  }>(),
  {
    readonly: false,
    appendRemark: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, unknown>]
  'update:remark': [value: string]
}>()

const { t } = useI18n()
const { getDict } = useDict()

const runtimeRef = ref<InstanceType<typeof FormRuntime>>()
const dynamicRef = ref<InstanceType<typeof OaDynamicForm>>()
const richFormRefs = new Map<string, FormInstance>()
const activeTab = ref('basic')

const modelProxy = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const remarkProxy = computed({
  get: () => props.remark ?? '',
  set: (v) => emit('update:remark', v),
})

function isRichTextField(row: OaFieldDefVO) {
  const component = (row.componentType || '').toLowerCase()
  return (
    row.fieldType === 'richText' ||
    component === 'richtext' ||
    component === 'rich-editor'
  )
}

const richTextFields = computed(() => props.fields.filter(isRichTextField))
const basicFields = computed(() => props.fields.filter((f) => !isRichTextField(f)))
const basicTabLabel = computed(() => t('common.basicInfo'))

function oaByCode(code: string): OaFieldDefVO | undefined {
  return props.fields.find((f) => f.fieldCode === code)
}

function labelOf(field: OaFieldDefVO) {
  const fromHost = props.resolveLabel?.(field)?.trim()
  if (fromHost) return fromHost
  return field.fieldName || field.fieldCode || ''
}

function placeholderOf(field: OaFieldDefVO) {
  return props.resolvePlaceholder?.(field)?.trim() || field.placeholder || ''
}

function richTabName(field: OaFieldDefVO) {
  return `rt-${field.fieldCode}`
}

function toFormField(field: OaFieldDefVO): FormFieldDef {
  return oaFieldToFormField(field)
}

function setRichFormRef(code: string, el: unknown) {
  if (el && typeof el === 'object' && 'validate' in (el as object)) {
    richFormRefs.set(code, el as FormInstance)
  } else {
    richFormRefs.delete(code)
  }
}

function richRulesOf(field: OaFieldDefVO): FormRules {
  if (!isYes(field.fieldRequired)) {
    return {}
  }
  const message =
    props.resolveRequiredMessage?.(field)?.trim() ||
    t('common.pleaseInput', { field: labelOf(field) })
  return {
    [field.fieldCode!]: [
      {
        validator: (_rule, value, callback) => {
          if (isBlankRichText(value)) {
            callback(new Error(message))
            return
          }
          callback()
        },
        trigger: 'change',
      },
    ],
  }
}

function updateRichField(code: string, value: unknown) {
  emit('update:modelValue', { ...props.modelValue, [code]: value })
}

const hostContext = computed<FormRuntimeHostContext>(() => ({
  // 附件 + 富文本内嵌图：统一 OA 上传配置 / infra 下载通道（见 docs/architecture/33）
  fileBizType: FILE_BIZ_OA_INSTANCE,
  fileDownloadScope: FILE_DOWNLOAD_SCOPE_INFRA,
  resolveLabel: (field: FormFieldDef) => {
    const oa = oaByCode(field.fieldCode)
    if (oa && props.resolveLabel) {
      const v = props.resolveLabel(oa)?.trim()
      if (v) return v
    }
    return field.fieldName || field.fieldCode
  },
  resolvePlaceholder: (field: FormFieldDef) => {
    const oa = oaByCode(field.fieldCode)
    if (oa && props.resolvePlaceholder) {
      const v = props.resolvePlaceholder(oa)?.trim()
      if (v) return v
    }
    return field.placeholder || ''
  },
  resolveRequiredMessage: (field: FormFieldDef) => {
    const oa = oaByCode(field.fieldCode)
    return oa ? props.resolveRequiredMessage?.(oa)?.trim() || '' : ''
  },
  resolveMaxLengthMessage: (field: FormFieldDef, max: number) => {
    const oa = oaByCode(field.fieldCode)
    return oa ? props.resolveMaxLengthMessage?.(oa, max)?.trim() || '' : ''
  },
}))

/** 布局 compose 时剔除 richText，避免正文仍嵌在基本信息画布里 */
const runtimeDocument = computed(() =>
  buildBoundRuntimeDocument({
    fields: richTextFields.value.length ? basicFields.value : props.fields,
    layoutJson: props.layoutSnapshotJson,
    readonly: props.readonly,
  }),
)

watch(
  () => richTextFields.value.map((f) => f.fieldCode).join(','),
  () => {
    activeTab.value = 'basic'
  },
)

async function validate() {
  let basicOk = true
  if (runtimeDocument.value) {
    basicOk = !!(await runtimeRef.value?.validate())
  } else {
    basicOk = !!(await dynamicRef.value?.validate?.())
  }
  if (!richTextFields.value.length) return basicOk

  let richOk = true
  for (const field of richTextFields.value) {
    const code = field.fieldCode!
    const form = richFormRefs.get(code)
    if (!form) {
      // 未挂载时仍按 model 校验必填
      const rules = richRulesOf(field)[code]
      if (Array.isArray(rules) && rules.length) {
        if (isBlankRichText(props.modelValue[code])) {
          richOk = false
          activeTab.value = richTabName(field)
          break
        }
      }
      continue
    }
    try {
      await form.validate()
    } catch {
      richOk = false
      activeTab.value = richTabName(field)
      break
    }
  }
  if (!basicOk && richOk) {
    activeTab.value = 'basic'
  }
  return basicOk && richOk
}

defineExpose({ validate })
</script>

<style scoped lang="scss">
.oa-form-layout-runtime {
  &__tabs {
    :deep(.el-tabs__content) {
      overflow: visible;
    }
  }

  &__rich-form {
    :deep(.el-form-item__content) {
      width: 100%;
    }
  }
}
</style>
