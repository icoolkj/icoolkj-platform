<template>
  <el-input
    v-if="resolvedComponent === 'input'"
    :model-value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    clearable
    :style="fieldStyle"
    @update:model-value="emit('update:modelValue', $event)"
    @keyup.enter="mode === 'query' ? emit('enter') : undefined"
  />
  <el-input-number
    v-else-if="resolvedComponent === 'input-number'"
    :model-value="modelValue as number"
    :min="formConfig.min ?? 0"
    :disabled="disabled"
    controls-position="right"
    style="width: 100%"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <el-input
    v-else-if="resolvedComponent === 'textarea'"
    :model-value="modelValue"
    type="textarea"
    :rows="formConfig.rows ?? 2"
    :maxlength="formConfig.maxLength"
    :show-word-limit="Boolean(formConfig.maxLength)"
    :placeholder="placeholder"
    :disabled="disabled"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <el-select
    v-else-if="resolvedComponent === 'select'"
    :model-value="dictModelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    clearable
    :style="fieldStyle"
    @update:model-value="emitDictValue"
  >
    <el-option v-for="opt in dictOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
  </el-select>
  <el-radio-group
    v-else-if="resolvedComponent === 'radio'"
    :model-value="dictModelValue"
    :disabled="disabled"
    @update:model-value="emitDictValue"
  >
    <el-radio v-for="opt in dictOptions" :key="opt.value" :value="opt.value">
      {{ opt.label }}
    </el-radio>
  </el-radio-group>
  <el-date-picker
    v-else-if="resolvedComponent === 'date'"
    :model-value="modelValue"
    type="date"
    value-format="YYYY-MM-DD"
    :placeholder="placeholder"
    clearable
    :style="fieldStyle"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <el-date-picker
    v-else-if="resolvedComponent === 'datetime'"
    :model-value="modelValue"
    type="datetime"
    :value-format="formConfig.valueFormat ?? 'YYYY-MM-DD HH:mm:ss'"
    :placeholder="placeholder"
    clearable
    style="width: 100%"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <el-date-picker
    v-else-if="resolvedComponent === 'daterange'"
    :model-value="dateRangeModel"
    type="daterange"
    value-format="YYYY-MM-DD"
    unlink-panels
    clearable
    style="width: 100%"
    :start-placeholder="dateRangeStartPlaceholder"
    :end-placeholder="dateRangeEndPlaceholder"
    :disabled="disabled"
    @update:model-value="emitDateRange"
  />
  <OrgRefField
    v-else-if="resolvedComponent === 'user'"
    mode="user"
    :model-value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <OrgRefField
    v-else-if="resolvedComponent === 'dept'"
    mode="dept"
    :model-value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <el-input
    v-else-if="resolvedComponent === 'password'"
    :model-value="modelValue as string"
    type="password"
    :placeholder="placeholder"
    :disabled="disabled"
    show-password
    @update:model-value="emit('update:modelValue', $event)"
  />
  <el-tree-select
    v-else-if="resolvedComponent === 'tree-select'"
    :model-value="modelValue"
    :data="treeData"
    :props="treeProps"
    check-strictly
    clearable
    :disabled="disabled"
    style="width: 100%"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <IconPicker
    v-else-if="resolvedComponent === 'icon-picker'"
    :model-value="(modelValue as string) ?? ''"
    :placeholder="placeholder"
    :disabled="disabled"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <AvatarPicker
    v-else-if="resolvedComponent === 'avatar-picker'"
    :model-value="(modelValue as string) ?? ''"
    :avatar-file-id="String(formModel?.avatarFileId ?? '')"
    :avatar-url-sm="String(formModel?.avatarUrlSm ?? '')"
    mode="admin"
    :target-user-id="String(formModel?.id ?? '')"
    :disabled="disabled"
    :display-name="String(formModel?.nickname || formModel?.username || '')"
    :gp="gpFn"
    @update:model-value="emit('update:modelValue', $event)"
    @update:avatar-file-id="updateFormField('avatarFileId', $event)"
    @uploaded="handleAvatarUploaded"
  />
  <AttachmentManager
    v-else-if="resolvedComponent === 'cover-picker'"
    :model-value="(modelValue as string) ?? ''"
    :preview-url="String(formModel?.coverPreviewUrl ?? '')"
    :disabled="disabled"
    :gp="gpFn"
    :biz-type="requireAttachmentBizType()"
    :download-scope="attachmentDownloadScope"
    mode="single"
    variant="image"
    @update:model-value="emit('update:modelValue', $event)"
    @update:preview-url="updateFormField('coverPreviewUrl', $event)"
  />
  <RichTextEditor
    v-else-if="resolvedComponent === 'rich-editor'"
    :model-value="(modelValue as string) ?? ''"
    :placeholder="placeholder"
    :disabled="disabled"
    :biz-type="richEditorBizType"
    :download-scope="richEditorDownloadScope"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <AttachmentManager
    v-else-if="resolvedComponent === 'file'"
    :model-value="normalizeAttachmentIds(modelValue)"
    :disabled="disabled"
    :biz-type="requireAttachmentBizType()"
    :download-scope="attachmentDownloadScope"
    mode="multiple"
    variant="list"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <el-checkbox-group
    v-else-if="resolvedComponent === 'checkbox'"
    :model-value="checkboxValues"
    :disabled="disabled"
    @update:model-value="emitCheckboxValues"
  >
    <el-checkbox v-for="opt in dictOptions" :key="opt.value" :value="opt.value">
      {{ opt.label }}
    </el-checkbox>
  </el-checkbox-group>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue'
import { useI18n } from 'vue-i18n'
import IconPicker from '@/components/IconPicker.vue'
import OrgRefField from '@/components/form-designer/runtime/OrgRefField.vue'
import { normalizeAttachmentIds } from '@/composables/useAttachmentUpload'
import {
  FILE_DOWNLOAD_SCOPE_INFRA,
  FILE_DOWNLOAD_SCOPE_NOTICE,
  type FileDownloadScope,
} from '@/constants/file'
import { NO, YES } from '@/constants/flags'
import type { UserProfile } from '@/types/profile'
import type { DictOption } from '@/types/system/dict'
import type { GenViewFormField, GenViewQueryField } from '@/types/tool/genView'

const AvatarPicker = defineAsyncComponent(() => import('@/components/AvatarPicker.vue'))
const AttachmentManager = defineAsyncComponent(
  () => import('@/components/attachment/AttachmentManager.vue'),
)
const RichTextEditor = defineAsyncComponent(() => import('@/components/RichTextEditor.vue'))

const props = defineProps<{
  config: GenViewQueryField | GenViewFormField
  modelValue?: unknown
  placeholder?: string
  mode?: 'query' | 'form'
  disabled?: boolean
  getDict?: (dictType: string) => DictOption[]
  /** Field-scoped options (e.g. product list); takes precedence over dictType when non-empty */
  getFieldOptions?: (field: string) => DictOption[] | undefined
  treeData?: Record<string, unknown>[]
  treeProps?: { label: string; value: string; children: string }
  formModel?: Record<string, unknown>
  gp?: (key: string, params?: Record<string, string | number>) => string
  /** 注册表适配：强制渲染指定 component id（legacy 分支） */
  forcedKind?: string
}>()

const formConfig = computed(() => props.config as GenViewFormField)

/**
 * 附件字段必须由业务 view.json / 调用方声明 bizType；
 * GenFormField 不内置 notice/OA 等业务码，避免公共层与业务耦合。
 */
function requireAttachmentBizType(): string {
  const code = formConfig.value.bizType?.trim()
  if (!code) {
    throw new Error(
      `[GenFormField] field "${formConfig.value.field}" (${formConfig.value.component}) requires config.bizType`,
    )
  }
  return code
}

/**
 * 富文本内嵌上传：优先透传 config.bizType（OA host / view.json）；
 * 未声明时不传，由 RichTextEditor 默认 notice_richtext。
 */
const richEditorBizType = computed(() => formConfig.value.bizType?.trim() || undefined)

/**
 * 富文本下载通道：显式 downloadScope 优先；
 * 否则 notice_* / 未声明 → notice（兼容公告 view.json）；其它业务（如 oa_instance）→ infra。
 */
const richEditorDownloadScope = computed<FileDownloadScope>(() => {
  if (formConfig.value.downloadScope === 'notice') return FILE_DOWNLOAD_SCOPE_NOTICE
  if (formConfig.value.downloadScope === 'infra') return FILE_DOWNLOAD_SCOPE_INFRA
  const biz = formConfig.value.bizType?.trim() || ''
  if (!biz || biz.startsWith('notice_')) return FILE_DOWNLOAD_SCOPE_NOTICE
  return FILE_DOWNLOAD_SCOPE_INFRA
})

/** 下载作用域：仅透传配置；未声明时用技术默认 infra（非业务绑定） */
const attachmentDownloadScope = computed<FileDownloadScope>(() =>
  formConfig.value.downloadScope === 'notice'
    ? FILE_DOWNLOAD_SCOPE_NOTICE
    : FILE_DOWNLOAD_SCOPE_INFRA,
)

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
  enter: []
  'avatar-uploaded': [payload: UserProfile]
}>()

const { t } = useI18n()
const dateRangeStartPlaceholder = computed(() => t('common.dateRangeStart'))
const dateRangeEndPlaceholder = computed(() => t('common.dateRangeEnd'))

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

const gpFn = computed(() => props.gp ?? ((key: string) => key))

function updateFormField(field: string, value: string) {
  if (!props.formModel) return
  // eslint-disable-next-line vue/no-mutating-props -- shared form bag
  props.formModel[field] = value
}

function handleAvatarUploaded(payload: UserProfile) {
  if (props.formModel) {
    /* eslint-disable vue/no-mutating-props -- shared form bag */
    props.formModel.avatarFileId = payload.avatarFileId
    props.formModel.avatarUrl = payload.avatarUrl
    props.formModel.avatarUrlSm = payload.avatarUrlSm
    /* eslint-enable vue/no-mutating-props */
  }
  emit('avatar-uploaded', payload)
}

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
  if (props.forcedKind) return props.forcedKind
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

const fieldStyle = computed(() => {
  if (props.mode === 'form') return undefined
  const width = 'width' in props.config ? props.config.width : undefined
  return width ? { width: `${width}px` } : { width: '200px' }
})

const treeProps = computed(
  () => props.treeProps ?? { label: 'label', value: 'id', children: 'children' },
)
</script>
