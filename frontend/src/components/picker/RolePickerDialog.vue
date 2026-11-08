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
    <div
      class="picker-layout"
      :class="multiTenant ? 'picker-layout--3col' : 'picker-layout--2col-transfer'"
    >
      <PickerListPanel v-if="multiTenant" :title="tenantTitle" tree>
        <el-tree
          :data="tenantTree"
          node-key="id"
          highlight-current
          default-expand-all
          :props="{ label: 'deptName', children: 'children' }"
          @node-click="handleTenantClick"
        />
      </PickerListPanel>

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
                  :disabled="isSelected(row)"
                  @click="addItem(row)"
                />
              </template>
            </el-table-column>
            <el-table-column
              :label="nameLabel"
              prop="roleName"
              min-width="120"
              show-overflow-tooltip
            />
            <el-table-column
              :label="codeLabel"
              prop="roleCode"
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
            @pagination="loadRoles"
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
                <PickerTransferButtons
                  mode="remove"
                  @click="removeItem(row.id || row.roleCode || '')"
                />
              </template>
            </el-table-column>
            <el-table-column
              :label="nameLabel"
              prop="roleName"
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
import { pageRole } from '@/api/system/role'
import type { RoleVO } from '@/types/system/role'
import type { TenantVO } from '@/types/tenant/tenant'
import PickerDialogShell from './PickerDialogShell.vue'
import PickerListPanel from './PickerListPanel.vue'
import PickerSearchBar from './PickerSearchBar.vue'
import PickerSimplePagination from './PickerSimplePagination.vue'
import PickerTransferButtons from './PickerTransferButtons.vue'
import { PICKER_DIALOG_WIDTH, PICKER_PAGE_SIZE, PICKER_SELECTED_PAGE_SIZE } from './constants'
import { useClientPageList } from './useClientPageList'
import { formatTenantLabel, TENANT_NODE_PREFIX } from './multiTenantTree'
import { usePickerTenants } from './usePickerTenants'

export type PickerRoleVO = RoleVO & {
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
    selected?: PickerRoleVO[]
    tenantTitle?: string
    roleListTitle?: string
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
    tenantTitle: 'Tenant',
    roleListTitle: 'Roles',
    selectedText: 'Selected',
    addCurrentText: 'Add current page',
    clearText: 'Clear all',
    searchPlaceholder: '',
    searchText: 'Search',
    resetText: 'Reset',
    cancelText: 'Cancel',
    confirmText: 'Confirm',
    actionLabel: 'Action',
    nameLabel: 'Role',
    codeLabel: 'Code',
    countUnit: '',
  },
)

const emit = defineEmits<{
  'update:modelValue': [boolean]
  confirm: [PickerRoleVO[]]
}>()

const loading = ref(false)
const keyword = ref('')
const rows = ref<PickerRoleVO[]>([])
const draft = ref<PickerRoleVO[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(PICKER_PAGE_SIZE)
const activeTenantId = ref<string>()
const selectedPageSize = PICKER_SELECTED_PAGE_SIZE

const {
  pageNo: selectedPageNo,
  pageRows: selectedPageRows,
  resetPage: resetSelectedPage,
} = useClientPageList(draft, selectedPageSize)

const { resolvedTenants, multiTenant } = usePickerTenants(props)

const tenantTree = computed(() =>
  resolvedTenants.value.map((tenant) => ({
    id: `${TENANT_NODE_PREFIX}${tenant.id}`,
    deptName: formatTenantLabel(tenant),
    tenantId: tenant.id,
  })),
)

const pendingHeaderText = computed(() => {
  const countPart = props.countUnit ? `${total.value} ${props.countUnit}` : String(total.value)
  return `${props.roleListTitle} ${countPart}`
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
    await loadRoles()
  },
)

async function loadRoles() {
  const tenantId = multiTenant.value ? activeTenantId.value : resolvedTenants.value[0]?.id
  loading.value = true
  try {
    const res = await pageRole({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      ...(tenantId ? { tenantId } : {}),
      roleName: keyword.value.trim() || undefined,
    })
    const tenant = resolvedTenants.value.find((item) => item.id === tenantId)
    const tenantLabel = tenant ? formatTenantLabel(tenant) : ''
    rows.value = (res.data.data?.list ?? []).map((role) => ({
      ...role,
      tenantId,
      tenantLabel,
    }))
    total.value = res.data.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNo.value = 1
  void loadRoles()
}

function resetSearch() {
  keyword.value = ''
  handleSearch()
}

function handleTenantClick(node: { tenantId?: string; id?: string }) {
  activeTenantId.value = node.tenantId || node.id?.slice(TENANT_NODE_PREFIX.length)
  pageNo.value = 1
  void loadRoles()
}

function isSelected(row: PickerRoleVO) {
  return draft.value.some(
    (item) =>
      (!!item.id && !!row.id && item.id === row.id) ||
      (!!item.roleCode && !!row.roleCode && item.roleCode === row.roleCode),
  )
}

function addItem(item: PickerRoleVO) {
  if (isSelected(item)) return
  draft.value.push(item)
}

function addCurrentPage() {
  rows.value.forEach((item) => addItem(item))
}

function removeItem(id: string) {
  draft.value = draft.value.filter((item) => item.id !== id && item.roleCode !== id)
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
