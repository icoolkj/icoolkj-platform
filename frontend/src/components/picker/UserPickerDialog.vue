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
    <div class="picker-layout picker-layout--3col">
      <PickerListPanel :title="deptTitle" tree>
        <el-tree
          :data="deptTree"
          node-key="id"
          highlight-current
          default-expand-all
          :props="{ label: 'deptName', children: 'children', disabled: 'disabled' }"
          @node-click="handleDeptClick"
        />
      </PickerListPanel>

      <PickerListPanel grow flush>
        <template #header>
          <span>{{ pendingHeaderText }}</span>
          <el-button
            link
            type="primary"
            class="picker-header-action"
            :disabled="!pendingRows.length"
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
            :data="pendingRows"
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
                  @click="addUser(row)"
                />
              </template>
            </el-table-column>
            <el-table-column
              :label="nicknameLabel"
              prop="nickname"
              min-width="100"
              show-overflow-tooltip
            />
            <el-table-column
              :label="usernameLabel"
              prop="username"
              min-width="100"
              show-overflow-tooltip
            />
            <el-table-column
              :label="deptTitle"
              prop="deptName"
              min-width="110"
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
            @pagination="loadUsers"
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
                <PickerTransferButtons mode="remove" @click="removeUser(row.id!)" />
              </template>
            </el-table-column>
            <el-table-column
              :label="nicknameLabel"
              prop="nickname"
              min-width="90"
              show-overflow-tooltip
            />
            <el-table-column
              v-if="multiTenant"
              :label="tenantTitle"
              prop="tenantLabel"
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
import { pageUser } from '@/api/system/user'
import { pageDept } from '@/api/system/dept'
import { STATUS_NORMAL } from '@/constants/flags'
import type { UserVO } from '@/types/system/user'
import type { DeptVO } from '@/types/system/dept'
import type { TenantVO } from '@/types/tenant/tenant'
import PickerDialogShell from './PickerDialogShell.vue'
import PickerListPanel from './PickerListPanel.vue'
import PickerSearchBar from './PickerSearchBar.vue'
import PickerSimplePagination from './PickerSimplePagination.vue'
import PickerTransferButtons from './PickerTransferButtons.vue'
import { PICKER_DIALOG_WIDTH, PICKER_PAGE_SIZE, PICKER_SELECTED_PAGE_SIZE } from './constants'
import { useClientPageList } from './useClientPageList'
import {
  buildDeptTree,
  buildMultiTenantDeptTree,
  formatTenantLabel,
  isTenantTreeNode,
  tenantIdFromTreeNode,
  type ScopedDeptVO,
} from './multiTenantTree'
import { usePickerTenants } from './usePickerTenants'

export type PickerUserVO = UserVO & {
  tenantId?: string
  tenantLabel?: string
}

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    width?: string
    tenantId?: string
    tenants?: TenantVO[]
    title: string
    selected?: PickerUserVO[]
    deptTitle?: string
    tenantTitle?: string
    pendingTitle?: string
    selectedTitle?: string
    selectedText?: string
    addCurrentText?: string
    clearText?: string
    searchPlaceholder?: string
    searchText?: string
    resetText?: string
    cancelText?: string
    confirmText?: string
    actionLabel?: string
    nicknameLabel?: string
    usernameLabel?: string
    countUnit?: string
  }>(),
  {
    width: PICKER_DIALOG_WIDTH,
    selected: () => [],
    deptTitle: 'Department',
    tenantTitle: 'Tenant',
    pendingTitle: 'Pending',
    selectedTitle: 'Selected',
    selectedText: 'Selected',
    addCurrentText: 'Add current page',
    clearText: 'Clear all',
    searchPlaceholder: '',
    searchText: 'Search',
    resetText: 'Reset',
    cancelText: 'Cancel',
    confirmText: 'Confirm',
    actionLabel: 'Action',
    nicknameLabel: 'Name',
    usernameLabel: 'Account',
    countUnit: '',
  },
)

const emit = defineEmits<{
  'update:modelValue': [boolean]
  confirm: [PickerUserVO[]]
}>()

const loading = ref(false)
const keyword = ref('')
const pendingRows = ref<PickerUserVO[]>([])
const draft = ref<PickerUserVO[]>([])
const deptTree = ref<ScopedDeptVO[]>([])
const activeTenantId = ref<string>()
const activeDeptId = ref<string>()
const pageNo = ref(1)
const pageSize = ref(PICKER_PAGE_SIZE)
const total = ref(0)
const selectedPageSize = PICKER_SELECTED_PAGE_SIZE

const {
  pageNo: selectedPageNo,
  pageRows: selectedPageRows,
  resetPage: resetSelectedPage,
} = useClientPageList(draft, selectedPageSize)

const { resolvedTenants, multiTenant } = usePickerTenants(props)

const tenantLabelMap = computed(() => {
  const map = new Map<string, string>()
  resolvedTenants.value.forEach((tenant) => {
    if (tenant.id) map.set(tenant.id, formatTenantLabel(tenant))
  })
  return map
})

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
    activeTenantId.value = resolvedTenants.value[0]?.id
    activeDeptId.value = undefined
    await Promise.all([loadDepts(), loadUsers()])
  },
)

watch(resolvedTenants, async () => {
  if (!props.modelValue) return
  draft.value = []
  resetSelectedPage()
  pageNo.value = 1
  activeTenantId.value = resolvedTenants.value[0]?.id
  activeDeptId.value = undefined
  await Promise.all([loadDepts(), loadUsers()])
})

async function loadDepts() {
  const tenants = resolvedTenants.value
  if (!tenants.length) {
    // 无显式租户时按当前会话拉部门树
    const res = await pageDept({ pageNo: 1, pageSize: 500 })
    const list = res.data.data?.list ?? []
    deptTree.value = buildDeptTree(list)
    return
  }
  if (multiTenant.value) {
    const deptsByTenant = new Map<string, DeptVO[]>()
    await Promise.all(
      tenants.map(async (tenant) => {
        const res = await pageDept({ pageNo: 1, pageSize: 500, tenantId: tenant.id })
        deptsByTenant.set(tenant.id!, res.data.data.list ?? [])
      }),
    )
    deptTree.value = buildMultiTenantDeptTree(tenants, deptsByTenant)
    return
  }
  const res = await pageDept({ pageNo: 1, pageSize: 500, tenantId: tenants[0].id })
  deptTree.value = buildDeptTree(res.data.data.list ?? [], tenants[0].id)
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await pageUser({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      ...(activeTenantId.value ? { tenantId: activeTenantId.value } : {}),
      status: STATUS_NORMAL,
      username: keyword.value.trim() || undefined,
      deptSubtreeRootId: activeDeptId.value,
    })
    const tenantLabel = activeTenantId.value
      ? tenantLabelMap.value.get(activeTenantId.value) || ''
      : ''
    pendingRows.value = (res.data.data?.list ?? []).map((user) => ({
      ...user,
      tenantId: activeTenantId.value || user.tenantId,
      tenantLabel,
    }))
    total.value = res.data.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNo.value = 1
  loadUsers()
}

function resetSearch() {
  keyword.value = ''
  handleSearch()
}

function handleDeptClick(node: ScopedDeptVO) {
  if (node.isTenantNode || isTenantTreeNode(node.id)) {
    activeTenantId.value = tenantIdFromTreeNode(node)
    activeDeptId.value = undefined
  } else {
    activeTenantId.value = node.tenantId || activeTenantId.value
    activeDeptId.value = node.id
  }
  pageNo.value = 1
  loadUsers()
}

function isSelected(id?: string) {
  return !!id && draft.value.some((item) => item.id === id)
}

function addUser(user: PickerUserVO) {
  if (!user.id || isSelected(user.id)) return
  draft.value.push(user)
}

function addCurrentPage() {
  pendingRows.value.forEach((user) => addUser(user))
}

function removeUser(id: string) {
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
