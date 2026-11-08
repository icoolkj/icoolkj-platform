<template>
  <PickerDialogShell
    :model-value="modelValue"
    :title="title"
    :width="width"
    :cancel-text="cancelText"
    :confirm-text="confirmText"
    :selected-text="selectedText"
    :selected-count="draft.length"
    @update:model-value="emit('update:modelValue', $event)"
    @confirm="confirm"
  >
    <div class="picker-layout picker-layout--2col-transfer">
      <PickerListPanel grow flush>
        <template #header>
          <span>{{ pendingHeaderText }}</span>
          <el-button
            link
            type="primary"
            class="picker-header-action"
            :disabled="!rows.length"
            @click="addCurrentPage"
          >
            {{ addCurrentText }}
          </el-button>
        </template>

        <PickerSearchBar
          v-model="keyword"
          :placeholder="searchPlaceholder"
          :search-text="searchText"
          :reset-text="resetText"
          @search="handleQuery"
          @reset="resetSearch"
        />

        <div class="picker-table-wrap">
          <el-table
            v-loading="loading"
            :data="rows"
            height="380"
            stripe
            border
            size="small"
            class="picker-transfer-table"
          >
            <el-table-column :label="actionLabel" width="56" align="center" fixed="left">
              <template #default="{ row }">
                <PickerTransferButtons
                  mode="add"
                  :disabled="isSelected(row.id)"
                  @click="addItem(row)"
                />
              </template>
            </el-table-column>
            <el-table-column
              :label="nameLabel"
              prop="tenantName"
              min-width="160"
              show-overflow-tooltip
            />
            <el-table-column
              :label="codeLabel"
              prop="tenantCode"
              min-width="120"
              show-overflow-tooltip
            />
            <el-table-column :label="typeLabel" prop="tenantType" width="120" align="center" />
          </el-table>
        </div>

        <template #footer>
          <Pagination
            v-show="total > 0"
            v-model:page="pageNo"
            v-model:limit="pageSize"
            :total="total"
            @pagination="loadData"
          />
        </template>
      </PickerListPanel>

      <PickerListPanel flush class="picker-panel--selected">
        <template #header>
          <span>{{ selectedText }}: {{ draft.length }}</span>
          <el-button
            link
            type="danger"
            class="picker-header-action"
            :disabled="!draft.length"
            @click="clearDraft"
          >
            {{ clearText }}
          </el-button>
        </template>

        <div class="picker-table-wrap picker-table-wrap--auto">
          <el-table
            :data="selectedPageRows"
            max-height="340"
            stripe
            border
            size="small"
            class="picker-transfer-table"
          >
            <el-table-column :label="actionLabel" width="56" align="center" fixed="left">
              <template #default="{ row }">
                <PickerTransferButtons mode="remove" @click="removeItem(row.id!)" />
              </template>
            </el-table-column>
            <el-table-column
              :label="nameLabel"
              prop="tenantName"
              min-width="120"
              show-overflow-tooltip
            />
          </el-table>
        </div>

        <template #footer>
          <PickerSimplePagination
            v-model:page="selectedPageNo"
            :total="draft.length"
            :page-size="selectedPageSize"
          />
        </template>
      </PickerListPanel>
    </div>
  </PickerDialogShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import Pagination from '@/components/Pagination.vue'
import { pageTenant } from '@/api/tenant/tenant'
import type { TenantVO } from '@/types/tenant/tenant'
import PickerDialogShell from './PickerDialogShell.vue'
import PickerListPanel from './PickerListPanel.vue'
import PickerSearchBar from './PickerSearchBar.vue'
import PickerSimplePagination from './PickerSimplePagination.vue'
import PickerTransferButtons from './PickerTransferButtons.vue'
import { PICKER_DIALOG_WIDTH, PICKER_PAGE_SIZE, PICKER_SELECTED_PAGE_SIZE } from './constants'
import { useClientPageList } from './useClientPageList'
import { isPlatformTenant, STATUS_NORMAL } from '@/constants/flags'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title: string
    width?: string
    selected?: TenantVO[]
    searchPlaceholder?: string
    searchText?: string
    resetText?: string
    cancelText?: string
    confirmText?: string
    selectedText?: string
    clearText?: string
    addCurrentText?: string
    pendingTitle?: string
    actionLabel?: string
    nameLabel?: string
    codeLabel?: string
    typeLabel?: string
    countUnit?: string
  }>(),
  {
    width: PICKER_DIALOG_WIDTH,
    selected: () => [],
    searchPlaceholder: '',
    searchText: 'Search',
    resetText: 'Reset',
    cancelText: 'Cancel',
    confirmText: 'Confirm',
    selectedText: 'Selected',
    clearText: 'Clear all',
    addCurrentText: 'Add current page',
    pendingTitle: 'Pending',
    actionLabel: 'Action',
    nameLabel: 'Tenant',
    codeLabel: 'Code',
    typeLabel: 'Type',
    countUnit: '',
  },
)

const emit = defineEmits<{
  'update:modelValue': [boolean]
  confirm: [TenantVO[]]
}>()

const loading = ref(false)
const keyword = ref('')
const rows = ref<TenantVO[]>([])
const draft = ref<TenantVO[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(PICKER_PAGE_SIZE)
const selectedPageSize = PICKER_SELECTED_PAGE_SIZE

const {
  pageNo: selectedPageNo,
  pageRows: selectedPageRows,
  resetPage: resetSelectedPage,
} = useClientPageList(draft, selectedPageSize)

const pendingHeaderText = computed(() => {
  const countPart = props.countUnit ? `${total.value} ${props.countUnit}` : String(total.value)
  return `${props.pendingTitle} ${countPart}`
})

watch(
  () => props.modelValue,
  async (open) => {
    if (!open) return
    draft.value = [...props.selected]
    keyword.value = ''
    pageNo.value = 1
    resetSelectedPage()
    await loadData()
  },
)

async function loadData() {
  loading.value = true
  try {
    const res = await pageTenant({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      status: STATUS_NORMAL,
      tenantName: keyword.value.trim() || undefined,
    })
    rows.value = (res.data.data.list ?? []).filter((item) => item.id && !isPlatformTenant(item.id))
    total.value = res.data.data.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  pageNo.value = 1
  loadData()
}

function resetSearch() {
  keyword.value = ''
  handleQuery()
}

function isSelected(id?: string) {
  return !!id && draft.value.some((item) => item.id === id)
}

function addItem(item: TenantVO) {
  if (!item.id || isSelected(item.id)) return
  draft.value.push(item)
}

function addCurrentPage() {
  rows.value.forEach((item) => addItem(item))
}

function removeItem(id: string) {
  draft.value = draft.value.filter((item) => item.id !== id)
}

function clearDraft() {
  draft.value = []
  resetSelectedPage()
}

function confirm() {
  emit('confirm', [...draft.value])
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
@use './picker.scss';
</style>
