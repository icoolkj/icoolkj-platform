<!-- 字段映射：简表 + GenFormShell 详情（跟随布局设置 dialog/drawer） -->
<template>
  <div class="oa-field-def-panel">
    <div class="toolbar">
      <el-button v-if="!readonly" type="primary" @click="openCreate">
        {{ t('common.add') }}
      </el-button>
      <el-tag v-if="versionStatus" type="info">v{{ versionNo }} · {{ versionStatus }}</el-tag>
    </div>
    <el-table :data="rows" border size="small" :max-height="maxHeight">
      <el-table-column v-if="!readonly" width="48" align="center">
        <template #default="{ $index }">
          <span
            class="drag-handle"
            draggable="true"
            @dragstart="onDragStart($index)"
            @dragover.prevent
            @drop="onDrop($index)"
          >
            ⋮⋮
          </span>
        </template>
      </el-table-column>
      <el-table-column v-if="tenantMode" :label="gf('fieldOrigin', 'label')" width="150">
        <template #default="{ row }">
          <div class="origin-tags">
            <el-tag size="small" :type="originTagType(row)">{{ originLabel(row) }}</el-tag>
            <el-tag v-if="isYes(row.platformLocked)" size="small" type="danger">
              {{ gp('fieldLockedTag') }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="fieldCode" :label="gf('fieldCode', 'label')" min-width="120" />
      <el-table-column prop="fieldName" :label="gf('fieldName', 'label')" min-width="120" />
      <el-table-column prop="fieldType" :label="gf('fieldType', 'label')" width="100" />
      <el-table-column
        prop="usedInCondition"
        :label="gf('usedInCondition', 'label')"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <el-tag v-if="isYes(row.usedInCondition)" size="small" type="success">
            {{ t('common.yes') }}
          </el-tag>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column v-if="!readonly" :label="t('common.actions')" width="140" fixed="right">
        <template #default="{ $index, row }">
          <el-button link type="primary" @click="openEdit($index)">
            {{ t('common.edit') }}
          </el-button>
          <el-button
            link
            type="danger"
            :disabled="tenantMode && isYes(row.platformLocked)"
            @click="removeRow($index)"
          >
            {{ t('common.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <GenFormShell
      v-model="shellVisible"
      :title="shellTitle"
      :width="640"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="108px">
        <el-form-item :label="gf('fieldCode', 'label')" prop="fieldCode">
          <el-input
            v-model="form.fieldCode"
            :placeholder="gf('fieldCode', 'placeholder')"
            :disabled="isFormLocked"
          />
        </el-form-item>
        <el-form-item :label="gf('fieldName', 'label')" prop="fieldName">
          <el-input v-model="form.fieldName" :placeholder="gf('fieldName', 'placeholder')" />
        </el-form-item>
        <el-form-item :label="gf('fieldType', 'label')" prop="fieldType">
          <el-select
            v-model="form.fieldType"
            style="width: 100%"
            :disabled="isFormLocked"
            @change="onFieldTypeChange"
          >
            <el-option v-for="opt in fieldTypes" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </el-form-item>
        <el-form-item :label="gf('componentType', 'label')" prop="componentType">
          <el-select
            v-model="form.componentType"
            style="width: 100%"
            :disabled="isFormLocked"
            @change="onComponentChange"
          >
            <el-option v-for="opt in componentTypes" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </el-form-item>
        <el-form-item :label="gf('dictType', 'label')" prop="dictType">
          <el-select
            v-model="form.dictType"
            filterable
            clearable
            style="width: 100%"
            :disabled="!needsDict(form.componentType) || isFormLocked"
            :placeholder="gf('dictType', 'placeholder')"
          >
            <el-option
              v-for="dt in dictTypeOptions"
              :key="dt.dictType"
              :label="`${dt.dictName} (${dt.dictType})`"
              :value="dt.dictType"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="gf('fieldRequired', 'label')">
          <el-switch v-model="form.fieldRequired" :active-value="YES" :inactive-value="NO" />
        </el-form-item>
        <el-form-item :label="gf('defaultValue', 'label')">
          <el-input
            v-model="form.defaultValue"
            :placeholder="gf('defaultValue', 'placeholder')"
            clearable
          />
        </el-form-item>
        <el-form-item :label="gf('promoteSlot', 'label')">
          <el-select
            v-model="form.promoteSlot"
            filterable
            clearable
            style="width: 100%"
            :disabled="isFileField(form) || isRichTextField(form)"
            :placeholder="gf('promoteSlot', 'placeholder')"
            @change="onPromoteSlotChange"
          >
            <el-option-group :label="gp('promoteSlotListGroup')">
              <el-option
                v-for="slot in listPromoteSlots"
                :key="slot"
                :label="slot"
                :value="slot"
                :disabled="isSlotTaken(slot)"
              />
            </el-option-group>
            <el-option-group :label="gp('promoteSlotExtraGroup')">
              <el-option
                v-for="slot in extraPromoteSlots"
                :key="slot"
                :label="slot"
                :value="slot"
                :disabled="isSlotTaken(slot)"
              />
            </el-option-group>
          </el-select>
          <p v-if="isRichTextField(form)" class="muted field-hint">{{ gp('richTextSlotHint') }}</p>
        </el-form-item>
        <el-form-item :label="gf('listSlotSync', 'label')">
          <el-switch
            v-model="form.listSlotSync"
            :active-value="YES"
            :inactive-value="NO"
            :disabled="!canListSync(form.promoteSlot) || isRichTextField(form)"
            @change="onListSyncChange"
          />
        </el-form-item>
        <el-form-item v-if="!tenantMode" :label="gf('platformLocked', 'label')">
          <el-switch v-model="form.platformLocked" :active-value="YES" :inactive-value="NO" />
        </el-form-item>
        <el-form-item :label="gf('usedInCondition', 'label')">
          <el-switch v-model="form.usedInCondition" :active-value="YES" :inactive-value="NO" />
        </el-form-item>
        <el-form-item :label="gf('usedInList', 'label')">
          <el-switch v-model="form.usedInList" :active-value="YES" :inactive-value="NO" />
        </el-form-item>
        <el-form-item :label="gf('fieldGroup', 'label')">
          <el-input
            v-model="form.fieldGroup"
            clearable
            :placeholder="gf('fieldGroup', 'placeholder')"
          />
        </el-form-item>
        <el-form-item :label="gf('sortNo', 'label')">
          <el-input-number v-model="form.sortNo" :min="0" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shellVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="applyForm">{{ t('common.confirm') }}</el-button>
      </template>
    </GenFormShell>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { listDictTypeOptions } from '@/api/system/dict'
import { DICT_SCOPE_OA } from '@/constants/dictScope'
import { isYes, NO, STATUS_NORMAL, YES } from '@/constants/flags'
import type { DictTypeVO } from '@/types/system/dict'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'
const { gf, gp } = useGenPageShellI18n('oaPlatformTemplate')

const props = withDefaults(
  defineProps<{
    versionNo?: number
    versionStatus?: string
    fields?: OaFieldDefVO[]
    tenantMode?: boolean
    /** 只读：隐藏新增/编辑/删除 */
    readonly?: boolean
    maxHeight?: number | string
  }>(),
  {
    maxHeight: 560,
    readonly: false,
  },
)

const { t } = useI18n()
const rows = ref<OaFieldDefVO[]>([])
const dictTypeOptions = ref<DictTypeVO[]>([])
const shellVisible = ref(false)
const editIndex = ref(-1)
const dragIndex = ref(-1)
const formRef = ref<FormInstance>()
const form = reactive<OaFieldDefVO>(emptyField())

const fieldTypes = [
  'string',
  'int',
  'decimal',
  'date',
  'datetime',
  'dateRange',
  'bool',
  'file',
  'ref',
  'richText',
]
const componentTypes = [
  'input',
  'textarea',
  'richText',
  'select',
  'radio',
  'checkbox',
  'date',
  'datetime',
  'dateRange',
  'number',
  'switch',
  'file',
  'user',
  'dept',
]
const dictComponents = new Set(['select', 'radio', 'checkbox'])
const listPromoteSlots = Array.from(
  { length: 10 },
  (_, i) => `idx_str_${String(i + 1).padStart(2, '0')}`,
)
const extraPromoteSlots = Array.from(
  { length: 90 },
  (_, i) => `idx_str_${String(i + 11).padStart(2, '0')}`,
)

const isFormLocked = computed(() => props.tenantMode === true && isYes(form.platformLocked))
const shellTitle = computed(() =>
  editIndex.value < 0 ? `${t('common.add')} · ${gf('fieldCode', 'label')}` : t('common.edit'),
)

const formRules = computed<FormRules>(() => ({
  fieldCode: [{ required: true, message: gf('fieldCode', 'label'), trigger: 'blur' }],
  fieldName: [{ required: true, message: gf('fieldName', 'label'), trigger: 'blur' }],
  fieldType: [{ required: true, message: gf('fieldType', 'label'), trigger: 'change' }],
}))

defineExpose({
  getFields: () => rows.value.map((f) => ({ ...f })),
})

watch(
  () => props.fields,
  (val) => {
    rows.value = (val ?? []).map((f) => ({ ...f }))
  },
  { immediate: true, deep: true },
)

onMounted(() => {
  void loadDictTypes()
})

function emptyField(): OaFieldDefVO {
  return {
    fieldCode: '',
    fieldName: '',
    fieldType: 'string',
    componentType: 'input',
    fieldRequired: NO,
    platformLocked: NO,
    usedInCondition: NO,
    usedInList: NO,
    listSlotSync: NO,
    sortNo: 0,
    colSpan: 1,
    status: STATUS_NORMAL,
  }
}

function assignForm(src: OaFieldDefVO) {
  Object.assign(form, emptyField(), src)
}

async function loadDictTypes() {
  const res = await listDictTypeOptions(DICT_SCOPE_OA)
  const list = [...(res.data.data ?? [])]
  const known = new Set(list.map((d) => d.dictType))
  for (const row of rows.value) {
    const code = row.dictType?.trim()
    if (code && !known.has(code)) {
      list.push({ dictType: code, dictName: code })
      known.add(code)
    }
  }
  dictTypeOptions.value = list
}

function needsDict(componentType?: string) {
  return !!componentType && dictComponents.has(componentType)
}

function canListSync(promoteSlot?: string) {
  return !!promoteSlot && listPromoteSlots.includes(promoteSlot)
}

function isSlotTaken(slot: string) {
  return rows.value.some((r, i) => i !== editIndex.value && r.promoteSlot === slot)
}

function originLabel(row: OaFieldDefVO) {
  const origin = row.originType || (isYes(row.platformLocked) ? 'platform_copy' : 'tenant_custom')
  if (origin === 'platform_copy') return gp('originPlatformCopy')
  if (origin === 'tenant_override') return gp('originTenantOverride')
  return gp('originTenantCustom')
}

function originTagType(row: OaFieldDefVO) {
  const origin = row.originType || (isYes(row.platformLocked) ? 'platform_copy' : 'tenant_custom')
  if (origin === 'platform_copy') return 'primary'
  if (origin === 'tenant_override') return 'warning'
  return 'success'
}

function isFileField(row: OaFieldDefVO) {
  return row.fieldType === 'file' || row.componentType === 'file'
}

function isRichTextField(row: OaFieldDefVO) {
  const component = (row.componentType || '').toLowerCase()
  return (
    row.fieldType === 'richText' ||
    component === 'richtext' ||
    component === 'rich-editor'
  )
}

function clearNonPromoteStorage() {
  if (!isFileField(form) && !isRichTextField(form)) return
  form.promoteSlot = undefined
  form.listSlotSync = NO
}

function onFieldTypeChange() {
  if (form.fieldType === 'file') {
    form.componentType = 'file'
  } else if (form.fieldType === 'richText') {
    form.componentType = 'richText'
  } else if (form.fieldType === 'dateRange') {
    form.componentType = 'dateRange'
  } else if (form.fieldType === 'ref' && form.componentType !== 'user' && form.componentType !== 'dept') {
    form.componentType = 'user'
  }
  clearNonPromoteStorage()
}

function onComponentChange() {
  if (!needsDict(form.componentType)) {
    form.dictType = undefined
  }
  if (form.componentType === 'file') {
    form.fieldType = 'file'
  } else if (form.componentType === 'richText') {
    form.fieldType = 'richText'
  } else if (form.componentType === 'dateRange') {
    form.fieldType = 'dateRange'
  } else if (form.componentType === 'user' || form.componentType === 'dept') {
    form.fieldType = 'ref'
  }
  clearNonPromoteStorage()
}

function onPromoteSlotChange() {
  if (!canListSync(form.promoteSlot)) {
    form.listSlotSync = NO
  }
}

function onListSyncChange() {
  if (isYes(form.listSlotSync) && !canListSync(form.promoteSlot)) {
    form.listSlotSync = NO
  }
}

function openCreate() {
  editIndex.value = -1
  assignForm({
    ...emptyField(),
    originType: props.tenantMode ? 'tenant_custom' : undefined,
    sortNo: rows.value.length,
  })
  shellVisible.value = true
}

function openEdit(index: number) {
  editIndex.value = index
  assignForm(rows.value[index])
  shellVisible.value = true
}

function removeRow(index: number) {
  rows.value.splice(index, 1)
  rewriteSortNo()
}

function onDragStart(index: number) {
  dragIndex.value = index
}

function onDrop(index: number) {
  if (dragIndex.value < 0 || dragIndex.value === index) return
  const [row] = rows.value.splice(dragIndex.value, 1)
  rows.value.splice(index, 0, row)
  dragIndex.value = -1
  rewriteSortNo()
}

function rewriteSortNo() {
  rows.value.forEach((row, index) => {
    row.sortNo = index
  })
}

async function applyForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const payload: OaFieldDefVO = { ...form }
  if (isRichTextField(payload)) {
    payload.fieldType = 'richText'
    payload.componentType = 'richText'
    payload.promoteSlot = undefined
    payload.listSlotSync = NO
    const otherRich = rows.value.some(
      (r, i) => i !== editIndex.value && isRichTextField(r),
    )
    if (otherRich) {
      ElMessage.warning(gp('richTextOnePerTemplate'))
      return
    }
  }
  if (editIndex.value < 0) {
    rows.value.push(payload)
  } else {
    rows.value[editIndex.value] = payload
  }
  shellVisible.value = false
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.origin-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.muted {
  color: var(--el-text-color-secondary);
}

.field-hint {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.4;
}

.drag-handle {
  cursor: grab;
  color: var(--el-text-color-secondary);
  user-select: none;
}
</style>
