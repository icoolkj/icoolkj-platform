<!-- 统一 OA 动态表单弹窗（按 categoryCode） -->
<template>
  <GenFormShell
    v-model="visible"
    :title="dialogTitle"
    :width="dialogWidth"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div v-loading="loading">
      <OaFormLayoutRuntime
        v-if="fields.length"
        ref="dynamicRef"
        v-model="formData"
        v-model:remark="remark"
        :fields="fields"
        :layout-snapshot-json="layoutSnapshotJson"
        :append-remark="appendRemark"
        :remark-label="gf('remark', 'label')"
        :remark-placeholder="gf('remark', 'placeholder')"
        :resolve-label="resolveLabel"
        :resolve-placeholder="resolvePlaceholder"
        :resolve-required-message="resolveRequiredMessage"
        :resolve-max-length-message="resolveMaxLengthMessage"
      />
      <el-empty v-else-if="!loading" :description="gp('formSchemaEmpty')" />
    </div>
    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" :disabled="!fields.length" @click="submit">
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import OaFormLayoutRuntime from '@/views/oa/components/OaFormLayoutRuntime.vue'
import {
  createOaInstance,
  getOaInstance,
  getOaInstanceFormSchema,
  updateOaInstance,
} from '@/api/oa/oaInstance'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'
import {
  categoryFieldLabel,
  categoryFieldPlaceholder,
  categoryFieldValidation,
  resolveCategoryGenI18n,
} from '@/utils/oa/categoryGenI18n'

const props = defineProps<{
  modelValue: boolean
  /** 新建时必填 */
  categoryCode?: string
  /** 编辑时单据 ID */
  instanceId?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const { gp, gf } = useGenPageShellI18n('officeApply')
const { t, locale } = useI18n()

const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit('update:modelValue', v),
})

const loading = ref(false)
const submitting = ref(false)
const fields = ref<OaFieldDefVO[]>([])
const formData = ref<Record<string, unknown>>({})
const remark = ref('')
const appendRemark = ref(true)
const layoutSnapshotJson = ref<string | null>(null)
const templateName = ref('')
const activeCategory = ref('')
const dynamicRef = ref<InstanceType<typeof OaFormLayoutRuntime>>()

const dialogTitle = computed(() => {
  if (props.instanceId) return gp('editTitle')
  return gp('addTitle', { name: templateName.value || gp('bizFallback') })
})

const hasRichTextField = computed(() =>
  fields.value.some((f) => {
    const component = (f.componentType || '').toLowerCase()
    return (
      f.fieldType === 'richText' ||
      component === 'richtext' ||
      component === 'rich-editor'
    )
  }),
)
/** 带正文 Tab 时加宽，给 wangEditor 留横向空间 */
const dialogWidth = computed(() => (hasRichTextField.value ? 860 : 560))

/** 模板 fieldName 优先；同名 gen 包（category→businessName）按 i18n-standard 兜底 */
const categoryGenMessages = computed(() => {
  const pack = resolveCategoryGenI18n(activeCategory.value)
  if (!pack) return null
  return pack[locale.value] ?? pack['zh-CN'] ?? pack['en-US'] ?? null
})

function resolveLabel(field: OaFieldDefVO) {
  const code = field.fieldCode || ''
  return categoryFieldLabel(categoryGenMessages.value, code, field.fieldName || code)
}

function resolvePlaceholder(field: OaFieldDefVO) {
  const code = field.fieldCode || ''
  return categoryFieldPlaceholder(categoryGenMessages.value, code)
}

function resolveRequiredMessage(field: OaFieldDefVO) {
  const code = field.fieldCode || ''
  return categoryFieldValidation(categoryGenMessages.value, code, 'required')
}

function resolveMaxLengthMessage(field: OaFieldDefVO, max: number) {
  const code = field.fieldCode || ''
  const tpl = categoryFieldValidation(categoryGenMessages.value, code, 'maxLength')
  return tpl ? tpl.replace(/\{max\}/g, String(max)) : ''
}

watch(
  () => [props.modelValue, props.instanceId, props.categoryCode] as const,
  async ([open]) => {
    if (!open) return
    loading.value = true
    fields.value = []
    formData.value = {}
    remark.value = ''
    layoutSnapshotJson.value = null
    templateName.value = ''
    activeCategory.value = props.categoryCode || ''
    try {
      if (props.instanceId) {
        const res = await getOaInstance(props.instanceId)
        const detail = res.data.data
        fields.value = detail.fields ?? []
        formData.value = { ...(detail.formData ?? {}) }
        remark.value = detail.remark ?? ''
        layoutSnapshotJson.value = detail.layoutSnapshotJson ?? null
        activeCategory.value = detail.categoryCode || props.categoryCode || ''
        appendRemark.value = !fields.value.some((f) => f.fieldCode === 'remark')
        templateName.value = detail.instanceTitle || ''
      } else if (props.categoryCode) {
        const res = await getOaInstanceFormSchema(props.categoryCode)
        const schema = res.data.data
        fields.value = schema.fields ?? []
        appendRemark.value = schema.appendRemark !== false
        layoutSnapshotJson.value = schema.layoutSnapshotJson ?? null
        templateName.value = schema.templateName || ''
        activeCategory.value = schema.categoryCode || props.categoryCode
        formData.value = {}
      }
    } finally {
      loading.value = false
    }
  },
)

async function submit() {
  const valid = await dynamicRef.value?.validate()
  if (!valid) return
  const category = activeCategory.value || props.categoryCode
  if (!props.instanceId && !category) return
  submitting.value = true
  try {
    const payload = {
      id: props.instanceId,
      formData: { ...formData.value },
      remark: appendRemark.value ? remark.value || undefined : undefined,
    }
    if (props.instanceId) {
      await updateOaInstance(payload)
    } else if (category) {
      await createOaInstance(category, payload)
    }
    ElMessage.success(t('common.success'))
    visible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>
