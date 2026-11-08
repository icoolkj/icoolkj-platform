<!-- 详情基本信息区：标量字段 descriptions + 附件分组 -->
<template>
  <div class="oa-instance-detail-basic">
    <el-descriptions
      v-if="scalarFields.length || remarkText"
      :column="DETAIL_COLUMNS"
      border
      class="oa-instance-detail-basic__desc"
      :style="{ '--oa-detail-label-width': `${labelWidthPx}px` }"
    >
      <el-descriptions-item
        v-for="field in scalarFields"
        :key="field.fieldCode"
        :label="resolveLabel(field)"
        :span="spanOf(field)"
      >
        <template v-if="field.dictType">
          <DictTag
            :options="getDict(field.dictType)"
            :value="dictTagValue(formData[field.fieldCode!])"
          />
        </template>
        <template v-else>
          <span class="oa-instance-detail-basic__value">{{ displayValue(field) }}</span>
        </template>
      </el-descriptions-item>
      <el-descriptions-item v-if="remarkText" :label="remarkLabel" :span="DETAIL_COLUMNS">
        <span class="oa-instance-detail-basic__value">{{ remarkText }}</span>
      </el-descriptions-item>
    </el-descriptions>
    <OaAttachmentPanel
      v-if="attachmentGroups.length"
      class="oa-instance-detail-basic__attachments"
      :groups="attachmentGroups"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DictTag from '@/components/DictTag.vue'
import {
  formatDateRangeDisplay,
  formatOrgRefDisplay,
} from '@/components/form-designer/runtime/orgRefValue'
import OaAttachmentPanel, {
  type OaAttachmentGroupInput,
} from '@/views/oa/components/OaAttachmentPanel.vue'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'
import type { DictOption } from '@/types/system/dict'

/** 详情默认两列；单项 span 对齐设计器 24 栅格映射结果 */
const DETAIL_COLUMNS = 2
/** 与 OA 动态表单 label-width(108) 同量级，按最长列名微调并夹紧 */
const LABEL_WIDTH_MIN = 96
const LABEL_WIDTH_MAX = 160
const LABEL_WIDTH_BASE = 108
const LABEL_CHAR_PX = 14
const LABEL_PAD_PX = 28

const props = defineProps<{
  scalarFields: OaFieldDefVO[]
  formData: Record<string, unknown>
  attachmentGroups: OaAttachmentGroupInput[]
  /** fieldCode → 设计器 24 栅格 span；缺省时走组件语义回落 */
  designerSpanByCode?: Record<string, number>
  remark?: string
  remarkLabel: string
  resolveLabel: (field: OaFieldDefVO) => string
  getDict: (dictType: string) => DictOption[]
}>()

const remarkText = computed(() => props.remark?.trim() || '')

/** 列名统一宽度：取本页最长标签估算，避免半格标签列忽宽忽窄 */
const labelWidthPx = computed(() => {
  let maxChars = 4
  for (const field of props.scalarFields) {
    maxChars = Math.max(maxChars, props.resolveLabel(field).trim().length)
  }
  if (remarkText.value) {
    maxChars = Math.max(maxChars, props.remarkLabel.trim().length)
  }
  const estimated = Math.max(LABEL_WIDTH_BASE, maxChars * LABEL_CHAR_PX + LABEL_PAD_PX)
  return Math.min(LABEL_WIDTH_MAX, Math.max(LABEL_WIDTH_MIN, estimated))
})

/**
 * 设计器 24 栅格 → descriptions 两列：
 * span ≥ 12（半行及以上宽）→ 整行；更窄 → 半行。
 * 无 layout 时：textarea / remark 整行，其余半行。
 */
function spanOf(field: OaFieldDefVO): number {
  const code = field.fieldCode || ''
  const fromDesigner = props.designerSpanByCode?.[code]
  if (fromDesigner != null && Number.isFinite(fromDesigner)) {
    return fromDesigner >= 12 ? DETAIL_COLUMNS : 1
  }
  const component = (field.componentType || '').toLowerCase()
  const codeLower = code.toLowerCase()
  if (component === 'textarea' || codeLower === 'remark' || codeLower.endsWith('remark')) {
    return DETAIL_COLUMNS
  }
  return 1
}

function dictTagValue(value: unknown): string | number | null | undefined {
  if (value == null) return value as null | undefined
  if (typeof value === 'string' || typeof value === 'number') return value
  return String(value)
}

function displayValue(field: OaFieldDefVO) {
  const v = props.formData[field.fieldCode!]
  if (v == null || v === '') return '—'
  const component = (field.componentType || '').toLowerCase()
  if (component === 'daterange' || component === 'date-range' || field.fieldType === 'dateRange') {
    return formatDateRangeDisplay(v) || '—'
  }
  if (component === 'user' || component === 'dept' || field.fieldType === 'ref') {
    return formatOrgRefDisplay(v) || '—'
  }
  if (Array.isArray(v)) return v.length ? v.map(String).join(', ') : '—'
  return String(v)
}
</script>

<style scoped lang="scss">
.oa-instance-detail-basic {
  &__desc {
    :deep(.el-descriptions__label) {
      width: var(--oa-detail-label-width, 108px);
      min-width: var(--oa-detail-label-width, 108px);
      max-width: var(--oa-detail-label-width, 108px);
      box-sizing: border-box;
      white-space: normal;
      word-break: break-word;
      vertical-align: top;
      color: var(--el-text-color-regular);
      font-weight: 500;
    }

    :deep(.el-descriptions__content) {
      vertical-align: top;
    }
  }

  &__attachments {
    margin-top: 16px;
  }

  &__value {
    white-space: pre-wrap;
    word-break: break-word;
  }
}
</style>
