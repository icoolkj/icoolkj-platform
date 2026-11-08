<!-- User / dept multi-select field for GenFormField / FormRuntime. Value: OrgRefItem[]. -->
<template>
  <div class="org-ref-field" :class="{ 'is-disabled': disabled }">
    <el-button type="primary" plain :disabled="disabled" @click="pickerOpen = true">
      {{ pickLabel }}
    </el-button>
    <div class="org-ref-field__tags">
      <el-tag
        v-for="item in items"
        :key="item.id"
        :closable="!disabled"
        @close="removeItem(item.id)"
      >
        {{ item.name }}
      </el-tag>
      <span v-if="!items.length" class="org-ref-field__placeholder">
        {{ placeholder || emptyHint }}
      </span>
    </div>

    <UserPickerDialog
      v-if="mode === 'user'"
      v-model="pickerOpen"
      :title="pickLabel"
      :selected="userSelected"
      :dept-title="labels.dept"
      :pending-title="labels.pending"
      :selected-title="labels.selected"
      :add-current-text="labels.addCurrent"
      :clear-text="labels.clear"
      :selected-text="labels.selectedCount"
      :search-placeholder="labels.userSearch"
      :search-text="labels.search"
      :reset-text="labels.reset"
      :cancel-text="labels.cancel"
      :confirm-text="labels.confirm"
      :action-label="labels.actions"
      :nickname-label="labels.nickname"
      :username-label="labels.username"
      @confirm="onUsersConfirm"
    />

    <DeptPickerDialog
      v-else
      v-model="pickerOpen"
      :title="pickLabel"
      :tree-title="labels.dept"
      :name-label="labels.dept"
      :selected-ids="items.map((i) => i.id)"
      :selected-text="labels.selectedCount"
      :clear-text="labels.clear"
      :cancel-text="labels.cancel"
      :confirm-text="labels.confirm"
      :action-label="labels.actions"
      @confirm="onDeptsConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import UserPickerDialog, {
  type PickerUserVO,
} from '@/components/picker/UserPickerDialog.vue'
import DeptPickerDialog, {
  type PickerDeptVO,
} from '@/components/picker/DeptPickerDialog.vue'
import {
  normalizeOrgRefValue,
  type OrgRefItem,
} from '@/components/form-designer/runtime/orgRefValue'

const props = defineProps<{
  mode: 'user' | 'dept'
  modelValue?: unknown
  placeholder?: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: OrgRefItem[]]
}>()

const { t } = useI18n()
const pickerOpen = ref(false)

const pickLabel = computed(() =>
  props.mode === 'user' ? t('common.orgPickUsers') : t('common.orgPickDepts'),
)

const emptyHint = computed(() =>
  props.mode === 'user' ? t('common.orgEmptyUsers') : t('common.orgEmptyDepts'),
)

const labels = computed(() => ({
  dept: t('common.orgDept'),
  pending: t('common.orgPending'),
  selected: t('common.orgSelected'),
  selectedCount: t('common.orgSelected'),
  addCurrent: t('common.orgAddCurrent'),
  clear: t('common.orgClear'),
  userSearch: t('common.orgUserSearch'),
  search: t('common.search'),
  reset: t('common.reset'),
  cancel: t('common.cancel'),
  confirm: t('common.confirm'),
  actions: t('common.actions'),
  nickname: t('common.orgNickname'),
  username: t('common.orgUsername'),
}))

const items = computed(() => normalizeOrgRefValue(props.modelValue))

const userSelected = computed<PickerUserVO[]>(() =>
  items.value.map((i) => ({
    id: i.id,
    nickname: i.name,
    username: i.name,
  })),
)

function removeItem(id: string) {
  emit(
    'update:modelValue',
    items.value.filter((i) => i.id !== id),
  )
}

function onUsersConfirm(rows: PickerUserVO[]) {
  emit(
    'update:modelValue',
    rows
      .filter((r) => r.id)
      .map((r) => ({
        id: String(r.id),
        name: String(r.nickname || r.username || r.id),
      })),
  )
}

function onDeptsConfirm(_ids: string[], rows: PickerDeptVO[]) {
  emit(
    'update:modelValue',
    rows
      .filter((r) => r.id)
      .map((r) => ({
        id: String(r.id),
        name: String(r.deptName || r.id),
      })),
  )
}
</script>

<style scoped>
.org-ref-field {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
}
.org-ref-field__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 24px;
  width: 100%;
}
.org-ref-field__placeholder {
  color: var(--el-text-color-placeholder);
  font-size: 13px;
}
.org-ref-field.is-disabled {
  opacity: 0.85;
}
</style>
