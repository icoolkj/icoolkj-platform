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
          @search="handleSearch"
          @reset="resetSearch"
        />

        <div class="picker-table-wrap">
          <el-table
            v-loading="loading"
            :data="rows"
            height="340"
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
              prop="postName"
              min-width="120"
              show-overflow-tooltip
            />
            <el-table-column
              :label="codeLabel"
              prop="postCode"
              min-width="100"
              show-overflow-tooltip
            />
          </el-table>
        </div>

        <template #footer>
          <Pagination
            v-show="total > 0"
            v-model:page="pageNo"
            v-model:limit="pageSize"
            :total="total"
            @pagination="loadPosts"
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
              prop="postName"
              min-width="90"
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
import { pagePost } from '@/api/system/post'
import { STATUS_NORMAL } from '@/constants/flags'
import type { PostVO } from '@/types/system/post'
import PickerDialogShell from './PickerDialogShell.vue'
import PickerListPanel from './PickerListPanel.vue'
import PickerSearchBar from './PickerSearchBar.vue'
import PickerSimplePagination from './PickerSimplePagination.vue'
import PickerTransferButtons from './PickerTransferButtons.vue'
import { PICKER_DIALOG_WIDTH, PICKER_PAGE_SIZE, PICKER_SELECTED_PAGE_SIZE } from './constants'
import { useClientPageList } from './useClientPageList'

export type PickerPostVO = PostVO

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    width?: string
    title: string
    selected?: PickerPostVO[]
    postListTitle?: string
    selectedText?: string
    addCurrentText?: string
    clearText?: string
    searchPlaceholder?: string
    searchText?: string
    resetText?: string
    cancelText?: string
    confirmText?: string
    actionLabel?: string
    nameLabel?: string
    codeLabel?: string
    countUnit?: string
  }>(),
  {
    width: PICKER_DIALOG_WIDTH,
    selected: () => [],
    postListTitle: 'Posts',
    selectedText: 'Selected',
    addCurrentText: 'Add current page',
    clearText: 'Clear all',
    searchPlaceholder: '',
    searchText: 'Search',
    resetText: 'Reset',
    cancelText: 'Cancel',
    confirmText: 'Confirm',
    actionLabel: 'Action',
    nameLabel: 'Post',
    codeLabel: 'Code',
    countUnit: '',
  },
)

const emit = defineEmits<{
  'update:modelValue': [boolean]
  confirm: [PickerPostVO[]]
}>()

const loading = ref(false)
const keyword = ref('')
const rows = ref<PickerPostVO[]>([])
const draft = ref<PickerPostVO[]>([])
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
  return `${props.postListTitle} ${countPart}`
})

watch(
  () => props.modelValue,
  async (open) => {
    if (!open) return
    draft.value = [...props.selected]
    keyword.value = ''
    pageNo.value = 1
    resetSelectedPage()
    await loadPosts()
  },
)

async function loadPosts() {
  loading.value = true
  try {
    const res = await pagePost({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      status: STATUS_NORMAL,
      postName: keyword.value.trim() || undefined,
    })
    rows.value = res.data.data?.list ?? []
    total.value = res.data.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNo.value = 1
  void loadPosts()
}

function resetSearch() {
  keyword.value = ''
  handleSearch()
}

function isSelected(id?: string) {
  return !!id && draft.value.some((item) => item.id === id)
}

function addItem(item: PickerPostVO) {
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
