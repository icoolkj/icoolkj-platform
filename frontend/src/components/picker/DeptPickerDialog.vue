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
    <div class="picker-dialog-body picker-dialog-body--compact">
      <div class="picker-layout picker-layout--tree-selected">
        <PickerListPanel :title="treeTitle" tree>
          <el-tree
            ref="treeRef"
            v-loading="loading"
            :data="deptTree"
            show-checkbox
            check-strictly
            node-key="id"
            default-expand-all
            :default-checked-keys="draftIds"
            :props="{ label: 'deptName', children: 'children', disabled: 'disabled' }"
            @check="syncDraftFromTree"
          />
        </PickerListPanel>

        <PickerListPanel flush class="picker-panel--selected">
          <template #header>
            <span>{{ selectedText }}: {{ draft.length }}</span>
            <el-button
              link
              type="danger"
              class="picker-header-action"
              :disabled="!draft.length"
              @click="clearSelection"
            >
              {{ clearText }}
            </el-button>
          </template>

          <div class="picker-table-wrap picker-table-wrap--auto">
            <el-table
              :data="selectedPageRows"
              max-height="420"
              stripe
              border
              size="small"
              class="picker-transfer-table"
            >
              <el-table-column :label="actionLabel" width="56" align="center" fixed="left">
                <template #default="{ row }">
                  <PickerTransferButtons mode="remove" @click="removeDept(row.id!)" />
                </template>
              </el-table-column>
              <el-table-column
                :label="nameLabel"
                prop="deptName"
                min-width="160"
                show-overflow-tooltip
              />
              <el-table-column
                v-if="multiTenant"
                :label="tenantTitle"
                prop="tenantLabel"
                min-width="140"
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
    </div>
  </PickerDialogShell>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { ElTree } from 'element-plus'
import { pageDept } from '@/api/system/dept'
import type { DeptVO } from '@/types/system/dept'
import type { TenantVO } from '@/types/tenant/tenant'
import PickerDialogShell from './PickerDialogShell.vue'
import PickerListPanel from './PickerListPanel.vue'
import PickerSimplePagination from './PickerSimplePagination.vue'
import PickerTransferButtons from './PickerTransferButtons.vue'
import { PICKER_DIALOG_WIDTH, PICKER_SELECTED_PAGE_SIZE } from './constants'
import { useClientPageList } from './useClientPageList'
import {
  buildDeptTree,
  buildMultiTenantDeptTree,
  formatTenantLabel,
  isTenantTreeNode,
  type ScopedDeptVO,
} from './multiTenantTree'
import { usePickerTenants } from './usePickerTenants'

export type PickerDeptVO = DeptVO & {
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
    treeTitle?: string
    tenantTitle?: string
    nameLabel?: string
    selectedIds?: string[]
    selectedText?: string
    clearText?: string
    cancelText?: string
    confirmText?: string
    actionLabel?: string
  }>(),
  {
    width: PICKER_DIALOG_WIDTH,
    selectedIds: () => [],
    treeTitle: 'Organization',
    tenantTitle: 'Tenant',
    nameLabel: 'Department',
    selectedText: 'Selected',
    clearText: 'Clear all',
    cancelText: 'Cancel',
    confirmText: 'Confirm',
    actionLabel: 'Action',
  },
)

const emit = defineEmits<{
  'update:modelValue': [boolean]
  confirm: [string[], PickerDeptVO[]]
}>()

const loading = ref(false)
const deptTree = ref<ScopedDeptVO[]>([])
const draftIds = ref<string[]>([])
const draft = ref<PickerDeptVO[]>([])
const flatDepts = ref<PickerDeptVO[]>([])
const treeRef = ref<InstanceType<typeof ElTree>>()
const selectedPageSize = PICKER_SELECTED_PAGE_SIZE

const {
  pageNo: selectedPageNo,
  pageRows: selectedPageRows,
  resetPage: resetSelectedPage,
} = useClientPageList(draft, selectedPageSize)

const { resolvedTenants, multiTenant } = usePickerTenants(props)

watch(
  () => props.modelValue,
  async (open) => {
    if (!open) return
    draftIds.value = [...props.selectedIds]
    resetSelectedPage()
    await loadDepts()
    syncDraftFromTree()
  },
)

async function loadDepts() {
  loading.value = true
  try {
    const tenants = resolvedTenants.value
    if (!tenants.length) {
      const res = await pageDept({ pageNo: 1, pageSize: 500 })
      flatDepts.value = res.data.data?.list ?? []
      deptTree.value = buildDeptTree(flatDepts.value)
      return
    }
    if (multiTenant.value) {
      const deptsByTenant = new Map<string, DeptVO[]>()
      await Promise.all(
        tenants.map(async (tenant) => {
          const res = await pageDept({ pageNo: 1, pageSize: 500, tenantId: tenant.id })
          const list = (res.data.data.list ?? []).map((item) => ({
            ...item,
            tenantId: tenant.id,
            tenantLabel: formatTenantLabel(tenant),
          }))
          deptsByTenant.set(tenant.id!, list)
        }),
      )
      flatDepts.value = Array.from(deptsByTenant.values()).flat()
      deptTree.value = buildMultiTenantDeptTree(tenants, deptsByTenant)
      return
    }
    const res = await pageDept({ pageNo: 1, pageSize: 500, tenantId: tenants[0].id })
    flatDepts.value = (res.data.data.list ?? []).map((item) => ({
      ...item,
      tenantId: tenants[0].id,
    }))
    deptTree.value = buildDeptTree(flatDepts.value, tenants[0].id)
  } finally {
    loading.value = false
  }
}

function syncDraftFromTree() {
  const ids = ((treeRef.value?.getCheckedKeys(false) as string[]) ?? []).filter(
    (id) => !isTenantTreeNode(id),
  )
  draftIds.value = ids
  draft.value = flatDepts.value.filter((item) => item.id && ids.includes(item.id))
}

function removeDept(id: string) {
  treeRef.value?.setChecked(id, false, false)
  syncDraftFromTree()
}

function clearSelection() {
  treeRef.value?.setCheckedKeys([])
  draftIds.value = []
  draft.value = []
  resetSelectedPage()
}

function confirm() {
  const ids = draftIds.value
  emit('confirm', ids, [...draft.value])
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
@use './picker.scss';
</style>
