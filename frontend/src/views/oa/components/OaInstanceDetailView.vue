<!-- OA 实例只读详情：descriptions + 附件列表 + 富文本正文（非禁用表单） -->
<template>
  <div class="oa-instance-detail-view">
    <el-tabs
      v-if="richTextFields.length"
      v-model="activeTab"
      class="oa-instance-detail-view__tabs"
    >
      <el-tab-pane :label="basicTabLabel" name="basic">
        <DetailBasicSection
          :scalar-fields="scalarFields"
          :form-data="formData"
          :attachment-groups="attachmentGroups"
          :designer-span-by-code="designerSpanByCode"
          :remark="remark"
          :remark-label="remarkLabelText"
          :resolve-label="labelOf"
          :get-dict="getDict"
        />
      </el-tab-pane>
      <el-tab-pane
        v-for="field in richTextFields"
        :key="field.fieldCode"
        :label="labelOf(field)"
        :name="`rt-${field.fieldCode}`"
        lazy
      >
        <div class="oa-instance-detail-view__content-block">
          <div class="oa-instance-detail-view__content-label">{{ labelOf(field) }}</div>
          <div class="oa-instance-detail-view__content-scroll">
            <RichHtmlContent
              :active="activeTab === `rt-${field.fieldCode}`"
              :html="richHtmlOf(field)"
              :download-scope="FILE_DOWNLOAD_SCOPE_INFRA"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <DetailBasicSection
      v-else
      :scalar-fields="scalarFields"
      :form-data="formData"
      :attachment-groups="attachmentGroups"
      :designer-span-by-code="designerSpanByCode"
      :remark="remark"
      :remark-label="remarkLabelText"
      :resolve-label="labelOf"
      :get-dict="getDict"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import RichHtmlContent from '@/components/RichHtmlContent.vue'
import { composeBoundDocument } from '@/components/form-designer/oaBridge'
import { resolveFieldSpan } from '@/components/form-designer/schemaUtils'
import { FILE_DOWNLOAD_SCOPE_INFRA } from '@/constants/file'
import { useDict } from '@/composables/useDict'
import {
  type OaAttachmentGroupInput,
} from '@/views/oa/components/OaAttachmentPanel.vue'
import DetailBasicSection from '@/views/oa/components/OaInstanceDetailBasicSection.vue'
import type { FormFieldDef } from '@/types/form-designer/schema'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'

const props = withDefaults(
  defineProps<{
    fields: OaFieldDefVO[]
    formData: Record<string, unknown>
    /** 设计器 layout 快照：用于详情跨列对齐填单 span */
    layoutSnapshotJson?: string | null
    remark?: string
    remarkLabel?: string
    resolveLabel?: (field: OaFieldDefVO) => string
  }>(),
  {
    formData: () => ({}),
  },
)

const { t } = useI18n()
const activeTab = ref('basic')
const basicTabLabel = computed(() => t('common.basicInfo'))
const remarkLabelText = computed(() => props.remarkLabel || t('common.remark'))

function fieldPermOf(field: OaFieldDefVO): 'R' | 'W' | 'H' {
  const p = field.fieldPerm
  return p === 'W' || p === 'H' ? p : 'R'
}

function isFileField(field: OaFieldDefVO) {
  return field.fieldType === 'file' || field.componentType === 'file'
}

function isRichTextField(field: OaFieldDefVO) {
  const component = (field.componentType || '').toLowerCase()
  return (
    field.fieldType === 'richText' ||
    component === 'richtext' ||
    component === 'rich-editor'
  )
}

function normalizeFileIds(raw: unknown): string[] {
  if (raw == null || raw === '') return []
  if (Array.isArray(raw)) {
    return raw.map((v) => String(v).trim()).filter(Boolean)
  }
  if (typeof raw === 'string') {
    const text = raw.trim()
    if (!text) return []
    if (text.startsWith('[')) {
      try {
        const parsed = JSON.parse(text) as unknown
        if (Array.isArray(parsed)) {
          return parsed.map((v) => String(v).trim()).filter(Boolean)
        }
      } catch {
        /* ignore */
      }
    }
    return [text]
  }
  return []
}

function labelOf(field: OaFieldDefVO) {
  const fromHost = props.resolveLabel?.(field)?.trim()
  if (fromHost) return fromHost
  return field.fieldName || field.fieldCode || ''
}

const visibleFields = computed(() =>
  props.fields.filter((f) => f.fieldCode && fieldPermOf(f) !== 'H'),
)

const richTextFields = computed(() => visibleFields.value.filter(isRichTextField))

const scalarFields = computed(() =>
  visibleFields.value.filter((f) => !isFileField(f) && !isRichTextField(f)),
)

const attachmentGroups = computed<OaAttachmentGroupInput[]>(() =>
  visibleFields.value
    .filter((f) => isFileField(f) && f.fieldCode)
    .map((f) => ({
      key: f.fieldCode!,
      label: labelOf(f),
      fileIds: normalizeFileIds(props.formData[f.fieldCode!]),
    }))
    .filter((g) => g.fileIds.length > 0),
)

/** 从 layout 快照还原设计器 span（fieldCode → 1..24） */
const designerSpanByCode = computed<Record<string, number>>(() => {
  const layout = props.layoutSnapshotJson?.trim()
  if (!layout || !props.fields.length) return {}
  try {
    const doc = composeBoundDocument({
      fields: props.fields,
      layoutJson: layout,
      options: { appendMissingFields: true, keepEmptyCards: true },
    })
    const out: Record<string, number> = {}
    const take = (field: FormFieldDef) => {
      if (!field.fieldCode) return
      out[field.fieldCode] = resolveFieldSpan(field)
    }
    for (const node of doc.nodes) {
      if (node.type === 'field') {
        take(node.field)
      } else if (node.type === 'row' || node.type === 'card') {
        for (const child of node.children) take(child)
      }
    }
    return out
  } catch {
    return {}
  }
})

const dictTypes = computed(() => {
  const types: string[] = []
  for (const field of scalarFields.value) {
    if (field.dictType) types.push(field.dictType)
  }
  return [...new Set(types)]
})
const { getDict } = useDict(dictTypes)

function richHtmlOf(field: OaFieldDefVO) {
  const raw = props.formData[field.fieldCode!]
  return typeof raw === 'string' ? raw : raw == null ? '' : String(raw)
}
</script>

<style scoped lang="scss">
.oa-instance-detail-view {
  &__tabs {
    :deep(.el-tabs__content) {
      overflow: visible;
    }
  }

  &__content-block {
    margin-top: 4px;
  }

  &__content-label {
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  /** 正文限高：溢出在内容区滚动，避免整页被撑长 */
  &__content-scroll {
    max-height: min(480px, 55vh);
    overflow-x: hidden;
    overflow-y: auto;
    padding: 12px 14px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 4px;
    background: var(--el-fill-color-blank);
  }
}
</style>
