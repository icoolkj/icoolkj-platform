<!--
  目录槽位附件表（对齐旧「配置目录 + 多槽位」上传）。
  业务页：传入 catalogCode，v-model 为 Record<profileCode, string | string[]>。
  特殊场景（头像裁剪 / 富文本内嵌 / 管理端存储对话框）请勿用本组件。
-->
<template>
  <div class="catalog-attachment" v-loading="loading">
    <div v-if="showHeader" class="catalog-attachment__header">
      <div class="catalog-attachment__col catalog-attachment__col--name">
        {{ headerLabel('attachmentCatalogName') }}
      </div>
      <div class="catalog-attachment__col catalog-attachment__col--req">
        {{ headerLabel('attachmentCatalogRequirements') }}
      </div>
      <div class="catalog-attachment__col catalog-attachment__col--files">
        {{ headerLabel('attachmentCatalogFiles') }}
      </div>
    </div>

    <div v-if="!loading && !slots.length" class="catalog-attachment__empty">
      {{ t('common.attachmentCatalogEmpty') }}
    </div>

    <div
      v-for="slot in slots"
      :key="slot.profileCode"
      class="catalog-attachment__row"
      :class="{ 'is-invalid': invalidCodes.has(slot.profileCode) }"
    >
      <div class="catalog-attachment__col catalog-attachment__col--name">
        <div class="catalog-attachment__label">
          <span v-if="slot.required" class="catalog-attachment__required">*</span>
          {{ slot.label }}
        </div>
        <div v-if="slot.remark" class="catalog-attachment__remark">{{ slot.remark }}</div>
      </div>

      <div class="catalog-attachment__col catalog-attachment__col--req">
        <p>
          <span class="catalog-attachment__req-key">{{ t('common.attachmentReqType') }}：</span>
          <span>{{ formatSlotTypeReq(slot) }}</span>
        </p>
        <p>
          <span class="catalog-attachment__req-key">{{ t('common.attachmentReqCount') }}：</span>
          <span>{{ formatSlotCountReq(slot, tCommon) }}</span>
        </p>
        <p>
          <span class="catalog-attachment__req-key">{{ t('common.attachmentReqSize') }}：</span>
          <span>{{ formatSlotSizeReq(slot, tCommon) }}</span>
        </p>
      </div>

      <div class="catalog-attachment__col catalog-attachment__col--files">
        <AttachmentManager
          :model-value="slotValue(slot)"
          :disabled="disabled"
          :biz-type="slot.profileCode"
          :biz-id="bizId"
          :ref-field="refFieldOf(slot)"
          :download-scope="downloadScope"
          :accept="slot.accept"
          :hint="slot.hint"
          :profile="slot.profile"
          :constraints="slot.constraints"
          :mode="slot.mode"
          :variant="forceVariant || slot.variant"
          :sensitivity-level="sensitivityLevel"
          :gp="gp"
          @update:model-value="(v) => setSlotValue(slot, v)"
          @uploaded="emitChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import AttachmentManager from './AttachmentManager.vue'
import { getFileCatalog } from '@/api/infra/fileProfile'
import {
  ATTACHMENT_MODE_MULTIPLE,
  FILE_DOWNLOAD_SCOPE_INFRA,
  FILE_SENSITIVITY_INTERNAL,
  type AttachmentVariant,
  type FileDownloadScope,
} from '@/constants/file'
import {
  buildCatalogSlots,
  flattenCatalogFileIds,
  formatSlotCountReq,
  formatSlotSizeReq,
  formatSlotTypeReq,
  validateCatalogAssets,
  type CatalogSlot,
} from '@/utils/fileCatalogSlots'

const props = withDefaults(
  defineProps<{
    /** 文件目录编码（infra_file_catalog.catalog_code） */
    catalogCode: string
    /** profileCode → fileId 或 fileId[] */
    modelValue?: Record<string, string | string[]>
    /** 业务主键；有值时上传会写 infra_file_ref */
    bizId?: string
    disabled?: boolean
    showHeader?: boolean
    downloadScope?: FileDownloadScope
    sensitivityLevel?: string
    /** 强制所有槽位同一展示形态；默认按槽位规则自动 image/list */
    forceVariant?: AttachmentVariant | null
    targetTenantId?: string
    gp?: (key: string, params?: Record<string, string | number>) => string
  }>(),
  {
    modelValue: () => ({}),
    showHeader: true,
    downloadScope: FILE_DOWNLOAD_SCOPE_INFRA,
    sensitivityLevel: FILE_SENSITIVITY_INTERNAL,
    forceVariant: null,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, string | string[]>]
  /** 槽位明细 + 展平 fileIds，便于表单提交 */
  change: [payload: { value: Record<string, string | string[]>; fileIds: string[]; slots: CatalogSlot[] }]
}>()

const { t, locale } = useI18n()
const loading = ref(false)
const slots = ref<CatalogSlot[]>([])
const invalidCodes = ref<Set<string>>(new Set())

const tCommon = (key: string, params?: Record<string, string | number>) =>
  t(`common.${key}`, params as Record<string, unknown>)

/** 优先 gp（业务 gen i18n / view 页文案），否则回落 common.* */
function headerLabel(key: string): string {
  const via = props.gp?.(key)
  if (via && via !== key) return via
  return t(`common.${key}`)
}

const localValue = computed({
  get: () => props.modelValue || {},
  set: (v) => emit('update:modelValue', v),
})

function refFieldOf(slot: CatalogSlot): string {
  return slot.profileCode
}

function slotValue(slot: CatalogSlot): string | string[] {
  const raw = localValue.value[slot.profileCode]
  if (slot.mode === ATTACHMENT_MODE_MULTIPLE) {
    if (Array.isArray(raw)) return raw
    if (raw == null || raw === '') return []
    return [String(raw)]
  }
  if (Array.isArray(raw)) return raw[0] || ''
  return raw ?? ''
}

function setSlotValue(slot: CatalogSlot, value: string | string[]) {
  const next = { ...localValue.value, [slot.profileCode]: value }
  localValue.value = next
  if (invalidCodes.value.has(slot.profileCode)) {
    const nextInvalid = new Set(invalidCodes.value)
    nextInvalid.delete(slot.profileCode)
    invalidCodes.value = nextInvalid
  }
  emitChange()
}

function emitChange() {
  const value = { ...localValue.value }
  emit('change', {
    value,
    fileIds: flattenCatalogFileIds(value),
    slots: slots.value,
  })
}

/** 必传 / 最小数量校验；失败提示并高亮槽位 */
function validate(): boolean {
  const result = validateCatalogAssets(slots.value, localValue.value)
  if (result.ok) {
    invalidCodes.value = new Set()
    return true
  }
  invalidCodes.value = new Set(result.missing.map((s) => s.profileCode))
  const names = result.missing.map((s) => s.label).join(locale.value.startsWith('zh') ? '、' : ', ')
  const viaGp = props.gp?.('attachmentRequired', { names })
  ElMessage.warning(
    viaGp && viaGp !== 'attachmentRequired'
      ? viaGp
      : t('common.attachmentRequired', { names }),
  )
  return false
}

async function loadCatalog() {
  const code = props.catalogCode?.trim()
  if (!code) {
    slots.value = []
    return
  }
  loading.value = true
  try {
    const res = await getFileCatalog(code, props.targetTenantId)
    slots.value = buildCatalogSlots(res.data.data?.items, locale.value, (key, params) =>
      t(`common.${key}`, params as Record<string, unknown>),
    )
    invalidCodes.value = new Set()
    emitChange()
  } catch (err) {
    slots.value = []
    ElMessage.error(
      err instanceof Error ? err.message : t('common.attachmentCatalogLoadFailed'),
    )
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.catalogCode, props.targetTenantId, locale.value] as const,
  () => {
    void loadCatalog()
  },
  { immediate: true },
)

defineExpose({
  reload: loadCatalog,
  slots,
  validate,
  flattenFileIds: () => flattenCatalogFileIds(localValue.value),
})
</script>

<style scoped lang="scss">
.catalog-attachment {
  width: 100%;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: var(--el-border-radius-base);
  overflow: hidden;
  background: var(--el-bg-color);
}

.catalog-attachment__header,
.catalog-attachment__row {
  display: flex;
  width: 100%;
}

.catalog-attachment__header {
  background: var(--el-color-primary-light-9);
  color: var(--el-text-color-primary);
  font-weight: 600;
  font-size: 13px;
  min-height: 44px;
  border-bottom: 1px solid var(--el-color-primary-light-7);
}

.catalog-attachment__row {
  border-top: 1px solid var(--el-border-color-lighter);
  min-height: 120px;
  background: var(--el-bg-color);

  &:first-of-type {
    border-top: none;
  }

  &.is-invalid {
    background: var(--el-color-danger-light-9);
    box-shadow: inset 3px 0 0 var(--el-color-danger);
  }
}

.catalog-attachment__col {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 12px 14px;
  border-right: 1px solid var(--el-border-color-lighter);

  &:last-child {
    border-right: none;
  }
}

.catalog-attachment__col--name {
  width: 20%;
  text-align: center;
  align-items: center;
}

.catalog-attachment__col--req {
  width: 22%;
}

.catalog-attachment__col--files {
  width: 58%;
  align-items: stretch;
}

/* 正文「要求」列用次级字色；表头三列保持统一主色加粗 */
.catalog-attachment__row .catalog-attachment__col--req {
  font-size: 13px;
  color: var(--el-text-color-regular);
  line-height: 1.7;

  p {
    margin: 0;
  }
}

.catalog-attachment__header .catalog-attachment__col {
  color: inherit;
  font-weight: inherit;
  font-size: inherit;
}

.catalog-attachment__label {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.catalog-attachment__required {
  color: var(--el-color-danger);
  margin-right: 2px;
}

.catalog-attachment__remark {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

.catalog-attachment__req-key {
  color: var(--el-text-color-secondary);
}

.catalog-attachment__empty {
  padding: 28px 16px;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  background: var(--el-fill-color-blank);
}

@media (max-width: 900px) {
  .catalog-attachment__header {
    display: none;
  }

  .catalog-attachment__row {
    flex-direction: column;
  }

  .catalog-attachment__col--name,
  .catalog-attachment__col--req,
  .catalog-attachment__col--files {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .catalog-attachment__col--files {
    border-bottom: none;
  }
}
</style>
